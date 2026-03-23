package one.theone.server.domain.member.repository;

import one.theone.server.domain.member.entity.SocialAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {

    // 구글 등 프로바이더와 해당 고유 ID로 이미 연결된 정보가 있는지 확인
    boolean existsByProviderAndProviderId(String provider, String providerId);

    // 특정 멤버의 소셜 인증 정보가 필요할 때 (memberId PK 직접 참조)
    Optional<SocialAuth> findByMemberId(Long memberId);

    // 프로바이더 정보로 소셜 인증 정보 조회
    Optional<SocialAuth> findByProviderAndProviderId(String provider, String providerId);
}
