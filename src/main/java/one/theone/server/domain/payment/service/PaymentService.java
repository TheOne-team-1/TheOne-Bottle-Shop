package one.theone.server.domain.payment.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.payment.repository.PaymentQueryRepository;
import one.theone.server.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentQueryRepository paymentQueryRepository;

}
