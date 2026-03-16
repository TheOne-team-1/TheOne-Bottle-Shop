package one.theone.server.domain.member.repository;

import one.theone.server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일 중복 체크용
    boolean existsByEmail(String email);

    // 이메일로 회원 조회 (로그인 시 사용)
    Optional<Member> findByEmail(String email);

    //해당 코드를 가진 회원이 있는지 여부를 boolean으로 반환
    boolean existsByRecommendCode(String recommendCode);

    //추천인 코드로 회원 조회
    //@param recommendCode 추천인 코드
    //@return 검색된 회원 Optional
    Optional<Member> findByRecommendCode(String recommendCode);
}
