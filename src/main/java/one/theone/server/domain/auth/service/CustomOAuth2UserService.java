package one.theone.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.SocialAuth;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.member.repository.SocialAuthRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final SocialAuthRepository socialAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String providerId = attributes.get("sub").toString();

        // Member 조회 또는 생성 (소셜 전용 정적 팩토리 메서드 호출)
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.createSocial(email, name)));

        // SocialAuth 확인 및 저장
        if (!socialAuthRepository.existsByProviderAndProviderId("GOOGLE", providerId)) {
            socialAuthRepository.save(SocialAuth.of("GOOGLE", providerId, member.getId()));
        }

        return new DefaultOAuth2User(
                // role이 Enum이므로 .name()을 붙여 String으로 변환
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes,
                "email"
        );
    }
}