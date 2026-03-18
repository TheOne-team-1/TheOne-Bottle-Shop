package one.theone.server.fixture;

import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.category.repository.CategoryRepository;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
public abstract class PaymentAndRefundTestFixture {

    @Autowired protected MemberRepository memberRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected CategoryDetailRepository categoryDetailRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderDetailRepository orderDetailRepository;
    @Autowired protected PaymentRepository paymentRepository;
    @Autowired protected PointRepository pointRepository;
    @Autowired protected PointLogRepository pointLogRepository;

    protected Long commonCategoryId;
    protected Long commonCategoryDetailId;
    protected Long commonProductId;

    protected final List<Long> memberIdList  = new ArrayList<>();
    protected final List<Long> orderIdList   = new ArrayList<>();
    protected final List<Long> productIdList = new ArrayList<>();

    @BeforeEach
    void setUpBase() {
        Category category = categoryRepository.save(Category.register("Whiskey", 1));
        commonCategoryId = category.getId();

        CategoryDetail detail = categoryDetailRepository.save(CategoryDetail.register(category.getId(), "Islay", 1));
        commonCategoryDetailId = detail.getId();

        Product product = productRepository.save(
                Product.register("testWhiskey", 100000L, new BigDecimal("75.000"), 700, commonCategoryDetailId, 10L));
        commonProductId = product.getId();
        productIdList.add(product.getId());
    }

    @AfterEach
    void tearDownBase() {
        pointLogRepository.deleteAll();
        memberIdList.forEach(id -> pointRepository.findByMemberId(id).ifPresent(pointRepository::delete));

        orderIdList.forEach(orderId -> {
            paymentRepository.findByOrderId(orderId).ifPresent(paymentRepository::delete);
            orderDetailRepository.deleteAll(orderDetailRepository.findByOrderId(orderId));
            orderRepository.deleteById(orderId);
        });

        productIdList.forEach(productRepository::deleteById);
        categoryDetailRepository.deleteById(commonCategoryDetailId);
        categoryRepository.deleteById(commonCategoryId);
        memberIdList.forEach(memberRepository::deleteById);

        memberIdList.clear();
        orderIdList.clear();
        productIdList.clear();
    }

    protected Member createMember() {
        Member member = memberRepository.save(Member.create(
                UUID.randomUUID() + "@test.com", "pwd", "TEST",
                "99990101", UUID.randomUUID().toString().substring(0, 8)));
        memberIdList.add(member.getId());
        return member;
    }

    protected CompleteOrderPaymentFixture createCompletedOrderAndPayment(Long memberId, Long finalAmount) {
        Order order = Order.create(
                memberId, null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, finalAmount, 0L, finalAmount,
                "testAddress1", "testAddress2");
        order.markCompleted();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        orderDetailRepository.save(
                OrderDetail.create(order.getId(), commonProductId, "testWhiskey", finalAmount, 1));

        Payment payment = Payment.register(order.getId(), finalAmount);
        payment.updateComplete();
        paymentRepository.save(payment);

        return new CompleteOrderPaymentFixture(order, payment);
    }

    public record CompleteOrderPaymentFixture(Order order, Payment payment) {}
}
