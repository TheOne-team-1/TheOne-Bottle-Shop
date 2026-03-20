package one.theone.server.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.chat.dto.request.ChatMessageSendRequest;
import one.theone.server.domain.chat.dto.response.ChatMessageResponse;
import one.theone.server.domain.chat.entity.SenderType;
import one.theone.server.domain.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/rooms/{roomId")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageSendRequest request,
            Principal principal
    ) {
        Long senderId = Long.valueOf(principal.getName());
        SenderType senderType = SenderType.CUSTOMER;

        ChatMessageResponse response = chatService.saveMessage(senderId, senderType, roomId, request);

        messagingTemplate.convertAndSend("/sub/chat/rooms" + roomId, response);
    }

}
