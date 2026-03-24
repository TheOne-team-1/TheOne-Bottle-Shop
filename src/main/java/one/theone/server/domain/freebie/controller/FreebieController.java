package one.theone.server.domain.freebie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.freebie.dto.request.FreebieCreateRequest;
import one.theone.server.domain.freebie.dto.request.FreebieUpdateRequest;
import one.theone.server.domain.freebie.dto.response.*;
import one.theone.server.domain.freebie.service.FreebieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FreebieController {

    private final FreebieService freebieService;

    @PostMapping("/admin/freebies")
    public ResponseEntity<BaseResponse<FreebieCreateResponse>> createFreebie(
            @Valid @RequestBody FreebieCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "사은품 생성 성공", freebieService.createFreebie(request)));
    }

    @GetMapping("/admin/freebies")
    public ResponseEntity<BaseResponse<PageResponse<FreebiesGetResponse>>> getFreebies(
            @RequestParam(defaultValue = "0") int page
            , @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 목록 조회 성공", freebieService.getFreebies(page, size)));
    }

    @GetMapping("/admin/freebies/{id}")
    public ResponseEntity<BaseResponse<FreebieGetResponse>> getFreebie(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 단건 조회 성공", freebieService.getFreebie(id)));
    }

    @PatchMapping("/admin/freebies/{id}")
    public ResponseEntity<BaseResponse<FreebieUpdateResponse>> updateFreebie(
            @PathVariable Long id,
            @Valid @RequestBody FreebieUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 수정 성공", freebieService.updateFreebie(id, request)));
    }

    @DeleteMapping("/admin/freebies/{id}")
    public ResponseEntity<BaseResponse<FreebieDeleteResponse>> deleteFreebie(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 삭제 성공", freebieService.deleteFreebie(id)));
    }
}
