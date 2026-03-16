package one.theone.server.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.*;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.service.EventService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
            EventsGetRequest request,
            @PageableDefault(page = 0, size = 10)Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 목록 조회 성공",
                eventService.getEvents(request, pageable, authentication)
        ));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<BaseResponse<EventGetResponse>> getEvent(
            @PathVariable Long eventId,
            Authentication authentication) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "이벤트 상세 조회 성공",
                eventService.getEvent(eventId, authentication)
        ));
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
