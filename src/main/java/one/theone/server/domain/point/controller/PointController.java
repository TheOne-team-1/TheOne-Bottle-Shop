package one.theone.server.domain.point.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.dto.PointAdjustResponse;
import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import one.theone.server.domain.point.service.PointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PointController {

    private final PointService pointService;

    @PostMapping("/admin/points/{memberId}")
    public ResponseEntity<BaseResponse<PointAdjustResponse>> adjustPoint(
            @PathVariable Long memberId,
            @Valid @RequestBody PointAdjustRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "포인트 조정 성공", pointService.adjustPoint(memberId, request)));
    }

    @GetMapping("/points")
    public ResponseEntity<BaseResponse<PageResponse<PointLogsGetResponse>>> getPointLogs(
            @AuthenticationPrincipal Long memberId,
            @ModelAttribute PointLogsGetRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "포인트 내역 조회 성공", pointService.getPointLogs(memberId, request, page, size)));
    }
}
