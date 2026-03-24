package one.theone.server.common.config.websocket;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.security.JwtProvider;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ChatExceptionEnum;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.repository.ChatRoomRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        System.out.println("STOMP COMMAND = " + accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            System.out.println("CONNECT Authorization = " + bearerToken);

            if (bearerToken == null || bearerToken.isBlank()) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_AUTH_MISSING);
            }

            if (!bearerToken.startsWith("Bearer ")) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_AUTH_FORMAT_INVALID);
            }

            String token = bearerToken.substring(7);

            if (!jwtProvider.validateToken(token)) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_AUTH_INVALID);
            }

            Long memberId = jwtProvider.getMemberId(token);
            String role = jwtProvider.getRole(token);

            System.out.println("CONNECT Member ID = " + memberId + ", role = " + role);

            if (memberId == null || role == null || role.isBlank()) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_AUTH_INFO_INVALID);
            }

            String finalRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(finalRole));

            // String.valueOf(memberId) 대신 memberId(Long) 직접 사용
            // 일반 HTTP 필터와 타입을 일치시켜서 권한 비교 시 오류 방지
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(memberId, null, authorities);

            accessor.setUser(authentication);
            System.out.println("CONNECT user set = " + accessor.getUser());

            // 변경된 accessor(user 정보 포함)를 메시지에 반영하여 리턴
            // 이렇게 해야 SUBSCRIBE 단계에서 유저 정보가 유지
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            String destination = accessor.getDestination();

            System.out.println("SUBSCRIBE user = " + principal);
            System.out.println("SUBSCRIBE destination = " + destination);

            if (principal == null || destination == null || destination.isBlank()) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_SUBSCRIBE_INVALID);
            }

            if (destination.startsWith("/sub/chat/rooms/")) {
                // principal.getName()을 안전하게 파싱
                Long memberId = Long.valueOf(principal.getName());
                Long roomId = extractRoomId(destination, "/sub/chat/rooms/");

                ChatRoom room = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_NOT_FOUND));

                // Long vs Long 비교 (equals 사용)
                boolean isCustomer = room.getCustomerId().equals(memberId);
                boolean isManager = room.getManagerId() != null && room.getManagerId().equals(memberId);

                if (!isCustomer && !isManager) {
                    throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ACCESS_DENIED);
                }
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            String destination = accessor.getDestination();

            System.out.println("SEND user = " + principal);
            System.out.println("SEND destination = " + destination);

            if (principal == null || destination == null || destination.isBlank()) {
                throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_WS_SEND_INVALID);
            }

            if (destination.startsWith("/pub/chat/rooms/")) {
                Long memberId = Long.valueOf(principal.getName());
                Long roomId = extractRoomId(destination, "/pub/chat/rooms/");

                ChatRoom room = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_NOT_FOUND));

                boolean isCustomer = room.getCustomerId().equals(memberId);
                boolean isManager = room.getManagerId() != null && room.getManagerId().equals(memberId);

                if (!isCustomer && !isManager) {
                    throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ACCESS_DENIED);
                }
            }
        }

        return message;
    }

    private Long extractRoomId(String destination, String prefix) {
        String roomId = destination.substring(prefix.length());
        return Long.valueOf(roomId);
    }
}