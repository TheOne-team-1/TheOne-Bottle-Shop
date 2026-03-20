package one.theone.server.common.config.websocket;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.security.JwtProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");

            if (bearerToken == null || bearerToken.isBlank()) {
                throw new IllegalArgumentException("웹소켓 인증 토큰이 없습니다");
            }

            if (!bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("웹소켓 인증 토큰 형식이 올바르지 않습니다");
            }

            String token = bearerToken.substring(7);

            if (!jwtProvider.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 웹소켓 인증 토큰입니다");
            }

            Long memberId = jwtProvider.getMemberId(token);
            String role =  jwtProvider.getRole(token);

            if (memberId == null || role == null || role.isBlank()) {
                throw new IllegalArgumentException("웹소켓 인증 정보가 올바르지 않습니다");
            }

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(String.valueOf(memberId), null, authorities);

            accessor.setUser(authentication);
        }

        return message;
    }
}
