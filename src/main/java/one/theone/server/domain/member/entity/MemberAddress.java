package one.theone.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import one.theone.server.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "member_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 삭제 시 deleted 필드를 true로, deleted_at을 현재시간으로 업데이트
@SQLDelete(sql = "UPDATE member_address SET deleted = true, deleted_at = NOW() WHERE id = ?")
// 조회 시 deleted가 false인 데이터만 가져오도록 제한
@SQLRestriction("deleted = false")
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 500)
    private String addressDetail;

    @Column(nullable = false)
    private boolean defaultYn = false;

    public static MemberAddress create(Long memberId, String address, String addressDetail, boolean defaultYn) {
        MemberAddress memberAddress = new MemberAddress();
        memberAddress.memberId = memberId;
        memberAddress.address = address;
        memberAddress.addressDetail = addressDetail;
        memberAddress.defaultYn = defaultYn;
        return memberAddress;
    }


    //배송지 정보 수정 메서드
    //BaseTimeEntity에 의해 updated_at은 자동으로 업데이트
    public void update(String address, String addressDetail, boolean defaultYn) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.defaultYn = defaultYn;
    }

    //기본 배송지 상태만 변경할 때 사용
    public void updateDefaultStatus(boolean isDefault) {
        this.defaultYn = isDefault;
    }
}
