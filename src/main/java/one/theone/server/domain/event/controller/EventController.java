package one.theone.server.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.*;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @PostMapping("/admin/events")
    public ResponseEntity<BaseResponse<EventCreateResponse>> createEvent(
            @Valid @RequestBody EventCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(
                HttpStatus.CREATED.name(),
                "이벤트 생성 성공",
                eventService.createEvent(request)));
    }

    @PatchMapping("/admin/events/{eventId}/status")
    public ResponseEntity<BaseResponse<EventStatusUpdateResponse>> updateEventStatus(
            @PathVariable Long eventId,
            @RequestBody EventStatusUpdateRequest request) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 상태 변경 성공",
                eventService.updateEventStatus(eventId, request)));
    }

    @GetMapping("/events")
    public ResponseEntity<BaseResponse<PageResponse<EventsGetResponse>>> getEvents(
            @RequestBody EventsGetRequest request
            , @RequestParam(defaultValue = "0") int page
            , @RequestParam(defaultValue = "10") int size
            , Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 목록 조회 성공",
                eventService.getEvents(request, page, size, isAdmin)
        ));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<BaseResponse<EventGetResponse>> getEvent(
            @PathVariable Long eventId,
            Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 상세 조회 성공",
                eventService.getEvent(eventId, isAdmin)
        ));
    }

    // 이벤트 API는 관리자 별 API 분리가 아닌 통합 시행
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }

    @DeleteMapping("/admin/events/{eventId}")
    public ResponseEntity<BaseResponse<EventDeleteResponse>> deleteEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 삭제 성공",
                eventService.deleteEvent(eventId)
        ));
    }
}
