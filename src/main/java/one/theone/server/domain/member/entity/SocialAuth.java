package one.theone.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SocialAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;   // GOOGLE
    private String providerId; // 구글 고유 sub 값
    private Long memberId;     // 연관관계 없이 PK만 저장

    // 정적 팩토리 패턴 적용
    public static SocialAuth of(String provider, String providerId, Long memberId) {
        return SocialAuth.builder()
                .provider(provider)
                .providerId(providerId)
                .memberId(memberId)
                .build();
    }
}
