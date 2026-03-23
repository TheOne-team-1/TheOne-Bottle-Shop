package one.theone.server.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.payment.dto.request.PaymentCreateRequest;
import one.theone.server.domain.payment.dto.response.PaymentConfirmResponse;
import one.theone.server.domain.payment.dto.response.PaymentCreateResponse;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.service.PaymentFacade;
import one.theone.server.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BaseResponse<PaymentCreateResponse>> createPayment(
            @RequestBody PaymentCreateRequest request
            , @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name()
                , "결제 성공", paymentFacade.createPayment(memberId, request)));
    }

    @PatchMapping("/{paymentId}/confirm")
    public ResponseEntity<BaseResponse<PaymentConfirmResponse>> confirmPayment(
            @PathVariable Long paymentId
            , @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name()
                , "확정 성공", paymentService.processConfirm(memberId, paymentId)));
    }
}
