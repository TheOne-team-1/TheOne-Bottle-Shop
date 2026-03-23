package one.theone.server.domain.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.payment.service.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessConfirmScheduler {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 5 * * *")
    public void ProcessingConfirm() {
        log.info("구매 확정 스케줄러 작동");

        List<Payment> paymentList = paymentRepository.findByStatusAndPayAtBefore(Payment.PaymentStatus.COMPLETED, LocalDateTime.now().minusDays(7));

        if (paymentList.isEmpty()) {
            return;
        }

        log.info("구매 확정 스케줄러 - {}건 확정 처리 시작", paymentList.size());

        int successCount = 0;
        int failCount = 0;

        for (Payment payment : paymentList) {
            try {
                Order order = orderRepository.findById(payment.getOrderId()).orElseThrow(() -> new IllegalStateException("주문 정보 없음 - orderId: " + payment.getOrderId()));

                if (order.getStatus() != OrderStatus.COMPLETED) {
                    continue;
                }

                paymentService.processConfirm(payment.getId(), order.getMemberId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("구매 확정 실패 - paymentId : {}, cause : {}", payment.getId(), e.getMessage());
            }
        }

        log.info("구매 확정 완료 - 성공 : {}건, 실패 : {}건", successCount, failCount);
    }
}
