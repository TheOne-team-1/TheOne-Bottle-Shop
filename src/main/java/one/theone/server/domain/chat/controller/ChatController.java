package one.theone.server.domain.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.response.ChatRoomResponse;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;
import one.theone.server.domain.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<BaseResponse<ChatRoomResponse>> createRoom(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatService.createRoom(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "채팅방 생성 성공", response));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<ChatRoomResponse>>> getMyRooms(
            @AuthenticationPrincipal Long memberId
    ) {
        List<ChatRoomResponse> response = chatService.getMyRooms(memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "내 채팅방 목록 조회 성공", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ChatRoomResponse>>> getAdminRooms(
            @RequestParam(required = false) ChatRoomStatus status
    ) {
        List<ChatRoomResponse> response = chatService.getAdminRooms(status);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "전체 채팅방 목록 조회 성공", response));
    }
}
