package one.theone.server.common.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.exception.domain.AuthExceptionEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            // 토큰이 아예 없는 경우 (A005)
            if (token == null) {
                request.setAttribute("exception", AuthExceptionEnum.ERR_EMPTY_TOKEN);
            }
            // 토큰이 존재하고 유효성 검증을 통과한 경우
            else if (jwtProvider.validateToken(token)) {
                Long memberId = jwtProvider.getMemberId(token);
                String role = jwtProvider.getRole(token);

                // ROLE_ 접두사를 붙여 권한 생성
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 토큰은 있지만 유효하지 않은 경우 (A004)
            else {
                request.setAttribute("exception", AuthExceptionEnum.ERR_INVALID_TOKEN);
            }

        } catch (ExpiredJwtException e) {
            // 만료된 토큰 처리 (A003)
            log.info("만료된 JWT 토큰입니다.");
            request.setAttribute("exception", AuthExceptionEnum.ERR_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            // 잘못된 형식이나 서명 오류 (A004)
            log.info("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            request.setAttribute("exception", AuthExceptionEnum.ERR_INVALID_TOKEN);
        } catch (Exception e) {
            // 그 외 기타 인증 실패 (A001)
            log.error("Security Context 인증 설정 실패: {}", e.getMessage());
            request.setAttribute("exception", AuthExceptionEnum.ERR_UNAUTHORIZED);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
