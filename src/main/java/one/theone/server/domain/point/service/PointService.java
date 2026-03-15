package one.theone.server.domain.point.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.PointExceptionEnum;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.dto.PointAdjustResponse;
import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.entity.PointUseDetail;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import one.theone.server.domain.point.repository.PointUseDetailRepository;
import one.theone.server.order.entity.Order;
import one.theone.server.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;
    private final OrderRepository orderRepository;
    private final PointUseDetailRepository pointUseDetailRepository;

    @Transactional
    public PointAdjustResponse adjustPoint(Long memberId, PointAdjustRequest request) {
//        memberRepository.findById(memberID)
//                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_MEMBER_NOT_FOUND));

        Long actualBalance = calculateActualBalance(memberId);
        validateBalance(actualBalance, request.amount());

        Point point = findOrCreatePoint(memberId);

        long newBalance = actualBalance + request.amount();
        PointLog log = PointLog.ofAdmin(memberId, request.amount(), newBalance);
        pointLogRepository.save(log);
        point.updateBalance(request.amount());

        return new PointAdjustResponse(memberId, request.amount(), newBalance);
    }

    @Transactional(readOnly = true)
    public PageResponse<PointLogsGetResponse> getPointLogs(Long memberId, PointLogsGetRequest request, Pageable pageable) {
        Page<PointLogsGetResponse> page = pointLogRepository.findPointLogs(memberId, request, pageable);
        return PageResponse.register(page);
    }

    @Transactional
    public void usePoint(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
        Long usePoint = order.getUsedPoint().longValue();

        Long actualBalance = calculateActualBalance(memberId);
        validateBalance(actualBalance, -usePoint);

        List<PointLog> earnedPointLogs = pointLogRepository.findAvailablePoints(memberId);

        long remaining = usePoint;
        for (PointLog earnedPointLog : earnedPointLogs) {
            long deductAmount =  Math.min(remaining, earnedPointLog.getRemainingAmount());

            PointUseDetail pointUseDetail = PointUseDetail.register(earnedPointLog.getId(), orderId, deductAmount);
            pointUseDetailRepository.save(pointUseDetail);
            earnedPointLog.deduct(deductAmount);

            remaining -= deductAmount;
            if(remaining == 0) break;
        }

        long newBalance = actualBalance - usePoint;
        PointLog usedPointLog = PointLog.ofUse(memberId, orderId, -usePoint, newBalance);
        pointLogRepository.save(usedPointLog);

        Point point = findOrCreatePoint(memberId);
        point.updateBalance(-usePoint);
    }

    @Transactional
    public void refundPoint(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
        Long usedPoint = order.getUsedPoint().longValue();

        Long actualBalance = calculateActualBalance(memberId);

        List<PointUseDetail> pointUseDetails = pointUseDetailRepository.findByOrderId(orderId);

        for (PointUseDetail pointUseDetail : pointUseDetails) {
            PointLog usedPointLog = pointLogRepository.findById(pointUseDetail.getPointLogId())
                    .orElseThrow(() -> new ServiceErrorException(PointExceptionEnum.ERR_POINT_LOG_NOT_FOUND));
            pointUseDetail.markRefunded();
            usedPointLog.restore(pointUseDetail.getAmount());
        }

        long newBalance = actualBalance + usedPoint;
        PointLog refundPointLog = PointLog.ofRefund(memberId, orderId, usedPoint, newBalance);
        pointLogRepository.save(refundPointLog);

        Point point = findOrCreatePoint(memberId);
        point.updateBalance(usedPoint);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePoint() {
        List<PointLog> pointLogsToExpire = pointLogRepository.findExpiredPoints();

        for (PointLog pointLogToExpire : pointLogsToExpire) {
            Long expiredPoint = pointLogToExpire.getRemainingAmount();

            Long actualBalance = calculateActualBalance(pointLogToExpire.getMemberId());

            long newBalance = actualBalance - expiredPoint;
            PointLog expiredPointLog = PointLog.ofExpired(pointLogToExpire.getMemberId(), -expiredPoint, newBalance);
            pointLogRepository.save(expiredPointLog);

            pointLogToExpire.deduct(expiredPoint);

            Point point = findOrCreatePoint(pointLogToExpire.getMemberId());
            point.updateBalance(-expiredPoint);
        }
    }

    private Point findOrCreatePoint(Long memberId) {
        return pointRepository.findByMemberId(memberId)
                .orElseGet(() -> pointRepository.save(Point.register(memberId)));
    }

    private Long calculateActualBalance(Long memberId) {
        return pointLogRepository.sumAmountByMemberId(memberId);
    }

    private void validateBalance(Long actualBalance, Long amount) {
        if (actualBalance + amount < 0) {
            throw new ServiceErrorException(PointExceptionEnum.ERR_INSUFFICIENT_POINT);
        }
    }
}
