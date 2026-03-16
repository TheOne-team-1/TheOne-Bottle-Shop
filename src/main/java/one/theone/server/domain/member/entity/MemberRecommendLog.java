package one.theone.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_recommend_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRecommendLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long voteMemberId; // 추천을 한 사람 (신규 가입자)

    @Column(nullable = false)
    private Long targetMemberId; // 추천을 받은 사람 (기존 회원)

    @Column(nullable = false)
    private LocalDateTime recommendAt;

    public static MemberRecommendLog create(Long voteMemberId, Long targetMemberId) {
        MemberRecommendLog log = new MemberRecommendLog();
        log.voteMemberId = voteMemberId;
        log.targetMemberId = targetMemberId;
        log.recommendAt = LocalDateTime.now();
        return log;
    }
}
