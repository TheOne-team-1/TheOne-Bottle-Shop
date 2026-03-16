package one.theone.server.domain.member.entity;

import one.theone.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE members SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwd;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 8)
    private String birthAt; // YYYYMMDD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;//관리자가 불필요 리뷰를 삭제할수있다는 내용을 참고함

    @Column(nullable = false, unique = true)
    private String recommendCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade grade = MemberGrade.BRONZE; // 기본값 BRONZE

    @Column(nullable = false)
    private LocalDateTime gradeAt = LocalDateTime.now(); // 등급 변경일

    @Column(nullable = false)
    private Long totalPayAmount = 0L;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deleted_at;


    //정적 팩토리 메서드를 통한 객체 생성
    public static Member create(String email, String encodedPassword, String name, String birthAt, String recommendCode) {
        Member member = new Member();
        member.email = email;
        member.passwd = encodedPassword;
        member.name = name;
        member.birthAt = birthAt;
        member.role = UserRole.USER;
        member.recommendCode = recommendCode;
        member.deleted = false;
        return member;
    }
    public void updatePassword(String encodedPassword) {

        this.passwd = encodedPassword;
    }
    public void addPayAmount(Long amount) {
        this.totalPayAmount += amount;
        updateGrade();
    }
    private void updateGrade() {
        MemberGrade newGrade = MemberGrade.of(this.totalPayAmount);

        if (this.grade != newGrade) {
            this.grade = newGrade;
            this.gradeAt = LocalDateTime.now();
        }
    }
}

