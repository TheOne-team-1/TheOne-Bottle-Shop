package one.theone.server.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.event.dto.EventCreateRequest;
import one.theone.server.domain.event.dto.EventCreateResponse;
import one.theone.server.domain.event.dto.EventStatusUpdateRequest;
import one.theone.server.domain.event.dto.EventStatusUpdateResponse;
import one.theone.server.domain.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                eventService.updateEventStatus(request)));
    }
}
