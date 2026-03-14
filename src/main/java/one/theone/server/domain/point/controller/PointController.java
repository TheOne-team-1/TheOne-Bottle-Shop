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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam Long memberId,
            @ModelAttribute PointLogsGetRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "포인트 내역 조회 성공", pointService.getPointLogs(memberId, request, pageable)));
    }
}
