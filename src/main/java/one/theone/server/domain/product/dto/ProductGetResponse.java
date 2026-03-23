package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.review.dto.ReviewResponse;

import java.math.BigDecimal;
import java.util.List;

public record ProductGetResponse(
        Long id,
        String name,
        Long price,
        Product.ProductStatus status,
        BigDecimal abv,
        int volumeMl,
        Long categoryId,
        Long categoryDetailId,
        Long quantity,
        BigDecimal rating,
        Long viewCount,
        List<ReviewResponse> top3Reviews
) {
    // QueryDSL용 기존 생성자 유지
    public ProductGetResponse(Long id, String name, Long price, Product.ProductStatus status, BigDecimal abv, int volumeMl,
                              Long categoryId, Long categoryDetailId, Long quantity, BigDecimal rating, Long viewCount) {
        this(id, name, price, status, abv, volumeMl, categoryId, categoryDetailId, quantity, rating, viewCount, List.of());
    }

    public ProductGetResponse withViewCountAndTop3Reviews(Long viewCount, List<ReviewResponse> top3Reviews) {
        return new ProductGetResponse(id, name,price, status, abv, volumeMl, categoryId, categoryDetailId, quantity, rating, viewCount, top3Reviews);
    }
}
