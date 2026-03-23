package one.theone.server.domain.member.repository;

import one.theone.server.domain.member.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    //특정 회원의 모든 배송지 목록을 조회
    //SQLRestriction("deleted = false") 설정에 의해 삭제되지 않은 데이터만 조회
    List<MemberAddress> findAllByMemberId(Long memberId);


    //특정 회원의 기본 배송지를 조회
    Optional<MemberAddress> findByMemberIdAndDefaultYnTrue(Long memberId);
}
