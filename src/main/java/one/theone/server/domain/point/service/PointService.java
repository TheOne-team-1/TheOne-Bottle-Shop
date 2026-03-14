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
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;

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
