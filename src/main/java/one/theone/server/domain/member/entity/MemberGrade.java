package one.theone.server.domain.member.entity;

import lombok.Getter;

@Getter
public enum MemberGrade {
    GOLD("골드", 3000000L),      // 300만 원 이상
    SILVER("실버", 2000000L),    // 200만 원 이하 (50만 초과 ~ 200만 이하)
    BRONZE("브론즈", 500000L);   // 50만 원 이하

    private final String description;
    private final long threshold;

    MemberGrade(String description, long threshold) {
        this.description = description;
        this.threshold = threshold;
    }

    public static MemberGrade of(long totalAmount) {
        if (totalAmount >= GOLD.threshold) return GOLD;
        if (totalAmount >= SILVER.threshold) return SILVER;
        return BRONZE;
    }
}
