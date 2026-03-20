package one.theone.server.domain.point.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.point.repository.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointPessimisticLockService {

    private final PointRepository pointRepository;
    private final PointService pointService;

    @Transactional
    public void usePoint(Long memberId, Long orderId) {
        pointRepository.findByMemberIdWithLock(memberId);
        pointService.usePoint(memberId, orderId);
    }

    @Transactional
    public void refundPoint(Long memberId, Long orderId) {
        pointRepository.findByMemberIdWithLock(memberId);
        pointService.refundPoint(memberId, orderId);
    }

    @Transactional
    public void earnPoint(Long memberId, Long orderId, Long finalAmount) {
        pointRepository.findByMemberIdWithLock(memberId);
        pointService.earnPoint(memberId, orderId, finalAmount);
    }

    @Transactional
    public void earnEventPoint(Long memberId, Long amount, String description) {
        pointRepository.findByMemberIdWithLock(memberId);
        pointService.earnEventPoint(memberId, amount, description);
    }
}
