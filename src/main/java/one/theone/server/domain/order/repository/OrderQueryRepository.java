package one.theone.server.domain.order.repository;

import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;
import one.theone.server.domain.order.dto.response.OrderListGetResponse;

import java.util.List;
import java.util.Optional;

public interface OrderQueryRepository {
    List<OrderListGetResponse> findOrderListByMemberId(Long memberId);
    Optional<OrderDetailGetResponse> findOrderDetail(Long orderId, Long memberId);
}
