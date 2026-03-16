package one.theone.server.domain.member.repository;

import one.theone.server.domain.member.entity.MemberRecommendLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


//추천인 로그 레포지토리
//연관관계 매핑 없는 ID 직접 참조 방식 지원
public interface MemberRecommendLogRepository extends JpaRepository<MemberRecommendLog, Long> {


    //특정 회원을 추천한 로그 목록을 조회 (나를 추천한 사람들)
    //@param targetMemberId 추천을 받은 회원 ID
    List<MemberRecommendLog> findAllByTargetMemberId(Long targetMemberId);


    //특정 회원이 누구를 추천했는지 로그 목록을 조회(내가 추천한 사람들)
    //@param voteMemberId 추천을 한 회원 ID
    List<MemberRecommendLog> findAllByVoteMemberId(Long voteMemberId);

    //특정 회원이 추천을 한 이력이 있는지 확인
    boolean existsByVoteMemberId(Long voteMemberId);
}
