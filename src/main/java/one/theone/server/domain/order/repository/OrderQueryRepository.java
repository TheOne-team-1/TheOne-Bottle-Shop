package one.theone.server.domain.order.repository;

import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;

import java.util.Optional;

public interface OrderQueryRepository {
    Optional<OrderDetailGetResponse> findOrderDetail(Long orderId, Long memberId);
}
