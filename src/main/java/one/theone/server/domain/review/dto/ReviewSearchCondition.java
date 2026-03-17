package one.theone.server.domain.review.dto;

public record ReviewSearchCondition(
        String keyword,        // 고객명, 상품명 검색 키워드
        Integer page,          // 페이지 번호 (기본값: 1)
        Integer size,          // 페이지당 개수 (기본값: 10)
        String sortBy,         // 정렬 기준 (rating, createdAt 등)
        String sortOrder,      // 정렬 순서 (asc, desc)
        Integer ratingFilter   // 평점 필터 (1~5)
) {
    // 콤팩트 생성자를 통해 기본값 설정 가능
    public ReviewSearchCondition {
        if (page == null) page = 1;
        if (size == null) size = 10;
        if (sortOrder == null) sortOrder = "desc";
    }
}
