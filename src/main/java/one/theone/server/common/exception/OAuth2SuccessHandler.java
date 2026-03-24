package one.theone.server.common.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.security.JwtProvider;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository; // memberId를 찾기 위해 주입

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object emailAttr = oAuth2User.getAttributes().get("email");
        String email = emailAttr != null ? emailAttr.toString() : null;

        // DB에서 해당 이메일의 유저 PK(ID)를 가져옴
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 추출한 권한 정보
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        // JwtProvider
        String accessToken = jwtProvider.createAccessToken(member.getId(), role); // (Long, String)
        String refreshToken = jwtProvider.createRefreshToken(); // 인자 없음

        // 프론트엔드 리다이렉트 (필요한 경로로 수정 가능)
        //String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/oauth2/redirect")
        //String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/index.html")
        String targetUrl = UriComponentsBuilder.fromUriString("http://theone-alb-200324328.ap-northeast-2.elb.amazonaws.com/index.html")

                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}