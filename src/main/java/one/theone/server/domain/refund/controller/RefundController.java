package one.theone.server.domain.refund.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.refund.dto.request.AdminRefundCreateRequest;
import one.theone.server.domain.refund.dto.request.RefundCreateRequest;
import one.theone.server.domain.refund.dto.response.RefundCreateResponse;
import one.theone.server.domain.refund.service.RefundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/api/refunds")
    public ResponseEntity<BaseResponse<RefundCreateResponse>> createRefund(
            @RequestBody RefundCreateRequest request
            , @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(
                HttpStatus.CREATED.name(), "환불 신청 성공", refundService.processRefund(memberId, request)));
    }

    @PostMapping("/api/admin/refunds")
    public ResponseEntity<BaseResponse<RefundCreateResponse>> createAdminRefund(
            @RequestBody AdminRefundCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(
                HttpStatus.CREATED.name(), "환불 처리 성공", refundService.processAdminRefund(request)));
    }
}
