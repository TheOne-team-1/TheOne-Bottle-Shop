package one.theone.server.domain.payment.service;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.category.repository.CategoryRepository;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.MemberGrade;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.dto.response.PaymentConfirmResponse;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import one.theone.server.domain.point.service.PointService;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.common.config.jpa.JpaAuditingConfig;
import one.theone.server.common.config.querydsl.QueryDslConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class, PaymentService.class, PointService.class})
@ActiveProfiles("test")
class PaymentServiceConfirmTest {
    @Autowired private PaymentService paymentService;

    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CategoryDetailRepository categoryDetailRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private PointLogRepository pointLogRepository;

    private final List<Long> memberIdList  = new ArrayList<>();
    private final List<Long> orderIdList   = new ArrayList<>();
    private final List<Long> productIdList = new ArrayList<>();

    private Long commonCategoryDetailId;
    private Long commonProductId;
    private Long commonCategoryId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.register("Whiskey", 1));
        commonCategoryId = category.getId();

        CategoryDetail detail = categoryDetailRepository.save(CategoryDetail.register(category.getId(), "Islay", 1));
        commonCategoryDetailId = detail.getId();

        Product product = productRepository.save(
                Product.register("testWhiskey", 100000L, new BigDecimal("75.000"), 700, commonCategoryDetailId, 100L)
        );
        commonProductId = product.getId();
        productIdList.add(product.getId());
    }

    @AfterEach
    void tearDown() {
        // PointLog, Point 삭제
        pointLogRepository.findAll().stream().filter(log -> memberIdList.contains(log.getMemberId()))
                .forEach(pointLog -> pointLogRepository.delete(pointLog));
        memberIdList.forEach(memberId -> pointRepository.findByMemberId(memberId).ifPresent(entity -> pointRepository.delete(entity)));

        // Payment, OrderDetail, Order 삭제
        paymentRepository.findAll().stream()
                .filter(payment_1 -> orderIdList.contains(payment_1.getOrderId()))
                .forEach(payment_2 -> paymentRepository.delete(payment_2));

        orderIdList.forEach(orderId -> {
            orderDetailRepository.deleteAll(orderDetailRepository.findByOrderId(orderId));
            orderRepository.deleteById(orderId);
        });

        productIdList.forEach(id -> productRepository.deleteById(id));
        categoryDetailRepository.deleteById(commonCategoryDetailId);
        categoryRepository.deleteById(commonCategoryId);

        memberIdList.forEach(id -> memberRepository.deleteById(id));

        memberIdList.clear();
        orderIdList.clear();
        productIdList.clear();
    }

    private Member createMember() {
        Member member = memberRepository.save(Member.create(
                UUID.randomUUID() + "@test.com", "pwd", "TEST",
                "99990101", UUID.randomUUID().toString().substring(0, 8)));
        memberIdList.add(member.getId());
        return member;
    }

    private PaymentFixture createCompletedPayment(Long memberId, Long finalAmount) {
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

        return new PaymentFixture(order, payment);
    }

    private record PaymentFixture(Order order, Payment payment) {}

    @Test
    @DisplayName("구매 확정 성공 - 주문 확정 전환")
    void processConfirm_success() {
        // given
        Member member = createMember();
        PaymentFixture fixture = createCompletedPayment(member.getId(), 100000L);

        // when
        PaymentConfirmResponse response = paymentService.processConfirm(
                fixture.payment().getId(), member.getId());

        // then
        assertThat(response.orderId()).isEqualTo(fixture.order().getId());
        assertThat(response.paymentId()).isEqualTo(fixture.payment().getId());
        assertThat(response.paymentUniqueId()).startsWith("PAY-");

        Order confirmed = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("구매 확정 성공 - 브론즈 회원 포인트 적립 및 구매 총액 증가")
    void processConfirm_earnPoint() {
        // given
        Member member = createMember();
        long finalAmount = 100000L;
        long expectedEarn = (long) Math.floor((double) finalAmount / 100);
        PaymentFixture fixture = createCompletedPayment(member.getId(), finalAmount);

        // when
        paymentService.processConfirm(fixture.payment().getId(), member.getId());

        // then - 포인트 잔액 확인
        Point point = pointRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(expectedEarn);

        // 구매 총액 증가 확인
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getTotalPayAmount()).isEqualTo(finalAmount);
        assertThat(updated.getGrade()).isEqualTo(MemberGrade.BRONZE);
    }

    @Test
    @DisplayName("구매 확정 성공 - 구매 총액이 등급 기준 초과 시 등급 자동 업그레이드")
    void processConfirm_gradeUpgrade() {
        // given
        Member member = createMember();
        member.addPayAmount(1990000L);
        memberRepository.save(member);

        long finalAmount = 50000L; // 확정 후 실버
        PaymentFixture fixture = createCompletedPayment(member.getId(), finalAmount);

        // earnPoint는 아직 브론즈 기준 적립
        long expectedEarn = (long) Math.floor((double) finalAmount / 100);

        // when
        paymentService.processConfirm(fixture.payment().getId(), member.getId());

        // then - 포인트는 브론즈 기준으로 적립
        Point point = pointRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(expectedEarn);

        // 등급은 SILVER로 업그레이드
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getGrade()).isEqualTo(MemberGrade.SILVER);
        assertThat(updated.getTotalPayAmount()).isEqualTo(1990000L + finalAmount);
    }

    @Test
    @DisplayName("구매 확정 실패 - 존재하지 않는 paymentId")
    void processConfirm_paymentNotFound() {
        Member member = createMember();
        Long nonExistPaymentId = 999999L;

        assertThatThrownBy(() -> paymentService.processConfirm(nonExistPaymentId, member.getId())).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 다른 회원의 주문 접근")
    void processConfirm_notMyOrder() {
        // given
        Member owner = createMember();
        Member other = createMember();
        PaymentFixture fixture = createCompletedPayment(owner.getId(), 100000L);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(fixture.payment().getId(), other.getId())
        ).isInstanceOf(ServiceErrorException.class);

        // 주문 상태 변화 없음
        Order unchanged = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("구매 확정 실패 - 결제가 완료 상태가 아닌 경우")
    void processConfirm_paymentNotCompleted() {
        // given - PENDING 상태 Payment 직접 생성
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment pendingPayment = Payment.register(order.getId(), 100000L); // PENDING 상태 유지
        paymentRepository.save(pendingPayment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(pendingPayment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 주문이 완료 상태가 아닌 경우")
    void processConfirm_orderNotCompleted() {
        // given
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");

        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(payment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 이미 확정 된 주문 재접근")
    void processConfirm_alreadyConfirmed() {
        // given - 이미 CONFIRMED 상태
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        order.markConfirmed(); // 이미 확정
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(payment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }
}
