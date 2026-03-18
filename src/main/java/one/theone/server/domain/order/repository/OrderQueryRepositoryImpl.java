package one.theone.server.domain.order.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;
import one.theone.server.domain.order.dto.response.OrderListGetResponse;
import one.theone.server.domain.order.entity.QOrder;
import one.theone.server.domain.order.entity.QOrderDetail;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    private final QOrder order = QOrder.order;
    private final QOrderDetail orderDetail = QOrderDetail.orderDetail;

    @Override
    public Optional<OrderDetailGetResponse> findOrderDetail(Long orderId, Long memberId) {
        List<OrderDetailGetResponse> result = queryFactory
                .from(order)
                .leftJoin(orderDetail).on(order.id.eq(orderDetail.orderId))
                .where(
                        order.id.eq(orderId),
                        order.memberId.eq(memberId)
                )
                .transform(GroupBy.groupBy(order.id).list(
                        com.querydsl.core.types.Projections.constructor(
                                OrderDetailGetResponse.class,
                                order.id,
                                order.orderNum,
                                order.status,
                                order.usedPoint,
                                order.totalAmount,
                                order.discountAmount,
                                order.finalAmount,
                                order.memberAddressSnap,
                                order.memberAddressDetailSnap,
                                order.createdAt,
                                GroupBy.list(
                                        com.querydsl.core.types.Projections.constructor(
                                                OrderDetailGetResponse.OrderDetailItemResponse.class,
                                                orderDetail.id,
                                                orderDetail.productId,
                                                orderDetail.productNameSnap,
                                                orderDetail.productPriceSnap,
                                                orderDetail.quantity,
                                                orderDetail.lineAmount
                                        )
                                )

                        )
                ));
        return result.stream().findFirst();
    }
}
