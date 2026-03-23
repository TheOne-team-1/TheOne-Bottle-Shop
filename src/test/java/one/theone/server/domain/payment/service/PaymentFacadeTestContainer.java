package one.theone.server.domain.payment.service;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.category.repository.CategoryRepository;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventDetail;
import one.theone.server.domain.event.entity.EventLog;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventDetailRepository;
import one.theone.server.domain.event.repository.EventLogRepository;
import one.theone.server.domain.event.repository.EventRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import one.theone.server.domain.freebie.entity.Freebie;
import one.theone.server.domain.freebie.repository.FreebieRepository;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.dto.request.PaymentCreateRequest;
import one.theone.server.domain.payment.dto.response.PaymentCreateResponse;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import one.theone.server.common.RedisTestContainer;

public class PaymentFacadeTestContainer extends RedisTestContainer {



    @Autowired
    private PaymentFacade paymentFacade;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryDetailRepository categoryDetailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private MemberCouponRepository memberCouponRepository;
    @Autowired
    private FreebieRepository freebieRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventDetailRepository eventDetailRepository;
    @Autowired
    private EventRewardRepository eventRewardRepository;
    @Autowired
    private EventLogRepository eventLogRepository;

    private final List<Long> memberIdList = new ArrayList<>();
    private final List<Long> orderIdList = new ArrayList<>();
    private final List<Long> productIdList = new ArrayList<>();
    private final List<Long> categoryIdList = new ArrayList<>();
    private final List<Long> categoryDetailIdList = new ArrayList<>();
    private final List<Long> couponIdList = new ArrayList<>();
    private final List<Long> freebieIdList = new ArrayList<>();
    private final List<Long> eventIdList = new ArrayList<>();

    private Long commonCategoryDetailId;
    private Long commonProductId;

    @BeforeEach
    void setUp() {
        // 공통 상품 카테고리 생성
        Category category = categoryRepository.save(Category.register("Whiskey", 1));
        categoryIdList.add(category.getId());

        CategoryDetail categoryDetail = categoryDetailRepository.save(CategoryDetail.register(category.getId(), "Islay", 1));
        categoryDetailIdList.add(categoryDetail.getId());
        commonCategoryDetailId = categoryDetail.getId();

        // 공통 상품 생성 (재고 10개)
        Product product = productRepository.save(
            Product.register("testProduct", 50000L, new BigDecimal("70.000"), 700, commonCategoryDetailId, 10L)
        );

        productIdList.add(product.getId());
        commonProductId = product.getId();
    }

    @AfterEach
    void tearDown() {
        // 이벤트 로그 삭제
        eventLogRepository.findAll().stream()
                .filter(log -> orderIdList.contains(log.getOrderId()))
                .forEach(entity -> eventLogRepository.delete(entity));

        // 이벤트 아이디 삭제
        eventIdList.forEach(eventId -> {
            eventRewardRepository.findByEventIdAndDeletedFalse(eventId).ifPresent(entity -> eventRewardRepository.delete(entity));
            eventDetailRepository.findByEventIdAndDeletedFalse(eventId).ifPresent(entity -> eventDetailRepository.delete(entity));
            eventRepository.deleteById(eventId);
        });

        freebieIdList.forEach(id -> freebieRepository.deleteById(id));

        // MemberCoupon, Coupon 삭제
        memberCouponRepository.findAll().stream()
                .filter(MemberCoupon_1 -> memberIdList.contains(MemberCoupon_1.getMemberId()))
                .forEach(MemberCoupon_2 -> memberCouponRepository.delete(MemberCoupon_2));
        couponIdList.forEach(id -> couponRepository.deleteById(id));

        // Payment, OrderDetail, Order 삭제
        paymentRepository.findAll().stream()
                .filter(payment_1 -> orderIdList.contains(payment_1.getOrderId()))
                .forEach(payment_2 -> paymentRepository.delete(payment_2));

        orderIdList.forEach(orderId -> {
            orderDetailRepository.deleteAll(orderDetailRepository.findByOrderId(orderId));
            orderRepository.deleteById(orderId);
        });

        // Product, CategoryDetail, Category 삭제
        productIdList.forEach(id -> productRepository.deleteById(id));
        categoryDetailIdList.forEach(id -> categoryDetailRepository.deleteById(id));
        categoryIdList.forEach(id -> categoryRepository.deleteById(id));

        memberIdList.clear();
        orderIdList.clear();
        productIdList.clear();
        categoryIdList.clear();
        categoryDetailIdList.clear();
        couponIdList.clear();
        freebieIdList.clear();
        eventIdList.clear();
    }

    private Member createMember() {
        Member member = memberRepository.save(Member.create(
                UUID.randomUUID() + "@test.com", "pwd", "TEST",
                "99990101", UUID.randomUUID().toString().substring(0, 8)));
        memberIdList.add(member.getId());
        return member;
    }

    // memberCouponId 없는 일반 주문
    private Order createOrder(Long memberId, Long totalAmount) {
        return createOrder(memberId, null, totalAmount);
    }

    // memberCouponId 포함 주문
    private Order createOrder(Long memberId, Long memberCouponId, Long totalAmount) {
        Order order = orderRepository.save(
                Order.create(
                        memberId
                        , memberCouponId
                        , UUID.randomUUID().toString().replace("-", "").substring(0, 20)
                        , 0L
                        , totalAmount
                        , 0L
                        , totalAmount
                        , "testAddress1"
                        , "testAddress2"
                )
        );

        orderIdList.add(order.getId());
        return order;
    }

    private void addOrderDetail(Long orderId, Long productId, String name, Long price, int qty) {
        orderDetailRepository.save(OrderDetail.create(orderId, productId, name, price, qty));
    }

    // 관리자 발급 쿠폰(AMOUNT, RATE 할인) 생성
    private Coupon createCoupon(Long minPrice, Long discountValue, Long availQuantity) {
        Coupon coupon = couponRepository.save(
                Coupon.register(
                    "testCoupon"
                    , Coupon.CouponUseType.AMOUNT
                    , minPrice
                    , discountValue
                    , availQuantity
                    , LocalDateTime.now().minusDays(1)
                    , LocalDate.now().plusDays(7)
                )
        );
        couponIdList.add(coupon.getId());
        return coupon;
    }

    // 회원에게 사용할 테스트 쿠폰 직접 발급 (ADMIN 발급 방식)
    private MemberCoupon issueMemberCoupon(Long memberId, Long couponId) {
        return memberCouponRepository.save(MemberCoupon.issuedByAdmin(memberId, couponId));
    }

    // 사은품 생성 (freebieCategoryDetailId = commonCategoryDetailId 재사용)
    private Freebie createFreebie(Long quantity) {
        Freebie freebie = freebieRepository.save(
                Freebie.register(commonCategoryDetailId, "testFreebie", quantity));
        freebieIdList.add(freebie.getId());
        return freebie;
    }

    // EventDetail/EventReward 는 직접 생성자 접근이 안 되므로 리플렉션 없이 빌더 역할 내부 클래스로 처리
    private record EventDetailTemporary(Long eventId, Long minPrice) {
        EventDetail build() {
            try {
                var constructor = EventDetail.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                EventDetail detail = constructor.newInstance();

                setField(detail, "eventId", eventId);
                setField(detail, "minPrice", minPrice);
                setField(detail, "deleted", false);
                return detail;
            } catch (Exception e) {
                throw new RuntimeException("EventDetail 생성 실패", e);
            }
        }

        private void setField(Object obj, String fieldName, Object value) throws Exception {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    private static class EventRewardBuilder {
        static EventReward freebie(Long eventId, Long freebieId) {
            try {
                var constructor = EventReward.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                EventReward reward = constructor.newInstance();

                setField(reward, "eventId", eventId);
                setField(reward, "rewardType", EventReward.EventRewardType.FREEBIE);
                setField(reward, "freebieId", freebieId);
                setField(reward, "deleted", false);
                return reward;
            } catch (Exception e) {
                throw new RuntimeException("EventReward (FREEBIE) 생성 실패", e);
            }
        }

        static EventReward coupon(Long eventId, Long couponId) {
            try {
                var constructor = EventReward.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                EventReward reward = constructor.newInstance();

                setField(reward, "eventId", eventId);
                setField(reward, "rewardType", EventReward.EventRewardType.COUPON);
                setField(reward, "couponId", couponId);
                setField(reward, "deleted", false);
                return reward;
            } catch (Exception e) {
                throw new RuntimeException("EventReward (COUPON) 생성 실패", e);
            }
        }

        private static void setField(Object obj, String fieldName, Object value) throws Exception {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    // OPEN 상태 AMOUNT_BUY 이벤트 생성 (minPrice 조건)
    private Event createAmountBuyEvent(Long minPrice) {
        Event event = Event.register(
                "testEvent"
                , LocalDateTime.now().minusDays(1)
                , LocalDateTime.now().plusDays(7)
                , Event.EventType.AMOUNT_BUY);
        event.updateStatus(Event.EventStatus.OPEN); // PENDING → OPEN
        Event saved = eventRepository.save(event);
        eventIdList.add(saved.getId());

        EventDetail detail = new EventDetailTemporary(saved.getId(), minPrice).build();
        eventDetailRepository.save(detail);

        return saved;
    }

    // EventReward 생성 - FREEBIE 보상
    private void createFreebieReward(Long eventId, Long freebieId) {
        EventReward reward = EventRewardBuilder.freebie(eventId, freebieId);
        eventRewardRepository.save(reward);
    }

    // EventReward 생성 - COUPON 보상
    private void createCouponReward(Long eventId, Long couponId) {
        EventReward reward = EventRewardBuilder.coupon(eventId, couponId);
        eventRewardRepository.save(reward);
    }

    @Test
    @DisplayName("결제 성공 - 기본 결제 흐름 (쿠폰·이벤트 없음)")
    void createPayment_success() {
        // given
        Member member = createMember();
        Order order = createOrder(member.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        Order completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        Product updatedProduct = productRepository.findById(commonProductId).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(9L);
    }

    @Test
    @DisplayName("결제 실패 - 다른 회원의 주문 결제 시도")
    void createPayment_notMyOrder() {
        // given
        Member owner = createMember();
        Member other = createMember();
        Order order = createOrder(owner.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when & then
        assertThatThrownBy(() ->
                paymentFacade.createPayment(other.getId(),
                        new PaymentCreateRequest(order.getId(), 50000L, 0L))
        ).isInstanceOf(ServiceErrorException.class);

        // 주문 상태 변화 없음
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("결제 실패 - 이미 완료된 주문 재결제 시도")
    void createPayment_alreadyCompleted() {
        // given
        Member member = createMember();
        Order order = createOrder(member.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        paymentFacade.createPayment(member.getId(),
                new PaymentCreateRequest(order.getId(), 50000L, 0L)); // 첫 결제 성공

        // when & then
        assertThatThrownBy(() ->
                paymentFacade.createPayment(member.getId(),
                        new PaymentCreateRequest(order.getId(), 50000L, 0L))
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("결제 실패 - 재고 부족 시 보상 트랜잭션으로 주문 상태 PENDING_PAYMENT 유지")
    void createPayment_insufficientStock_rollback() {
        // given - 재고 전부 소진
        Member member = createMember();
        Order order1 = createOrder(member.getId(), 500000L);
        addOrderDetail(order1.getId(), commonProductId, "testWhiskey", 50000L, 10);
        paymentFacade.createPayment(member.getId(),
                new PaymentCreateRequest(order1.getId(), 500000L, 0L));

        // 재고 0인 상태에서 추가 주문
        Order order2 = createOrder(member.getId(), 50000L);
        addOrderDetail(order2.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when & then
        assertThatThrownBy(() ->
                paymentFacade.createPayment(member.getId(),
                        new PaymentCreateRequest(order2.getId(), 50000L, 0L))
        ).isInstanceOf(ServiceErrorException.class);

        // 주문 상태는 PENDING_PAYMENT 유지 (보상 트랜잭션)
        assertThat(orderRepository.findById(order2.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("결제 성공 - 주문 쿠폰(정액 할인) 적용")
    void createPayment_withOrderCoupon_success() {
        // given
        Member member = createMember();

        // 10,000원 이상 구매 시 5,000원 할인 쿠폰
        Coupon coupon = createCoupon(10000L, 5000L, 100L);
        MemberCoupon memberCoupon = issueMemberCoupon(member.getId(), coupon.getId());

        // 50,000원 주문 + 쿠폰 적용
        Order order = createOrder(member.getId(), memberCoupon.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        Order completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getDiscountAmount()).isEqualTo(5000L);   // 쿠폰 할인 적용
        assertThat(completedOrder.getFinalAmount()).isEqualTo(45000L);     // 50,000 - 5,000

        // 쿠폰 상태 USED 변경 확인
        MemberCoupon usedCoupon = memberCouponRepository.findById(memberCoupon.getId()).orElseThrow();
        assertThat(usedCoupon.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("결제 실패 - 쿠폰 최소 주문금액 미달")
    void createPayment_withOrderCoupon_minPriceFail() {
        // given - 최소 금액 100,000원 쿠폰, 50,000원 주문
        Member member = createMember();
        Coupon coupon = createCoupon(100000L, 5000L, 100L);
        MemberCoupon memberCoupon = issueMemberCoupon(member.getId(), coupon.getId());

        Order order = createOrder(member.getId(), memberCoupon.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when & then
        assertThatThrownBy(() ->
                paymentFacade.createPayment(member.getId(),
                        new PaymentCreateRequest(order.getId(), 50000L, 0L))
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("결제 성공 - 이벤트 사은품 보상 지급 및 EventLog 기록")
    void createPayment_withEventFreebieReward_success() {
        // given
        Member member = createMember();

        // 사은품 (재고 5개)
        Freebie freebie = createFreebie(5L);

        // 30,000원 이상 구매 시 사은품 지급 이벤트
        Event event = createAmountBuyEvent(30000L);
        createFreebieReward(event.getId(), freebie.getId());

        // 50,000원 주문
        Order order = createOrder(member.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        // 사은품 재고 1 차감 확인
        Freebie updatedFreebie = freebieRepository.findById(freebie.getId()).orElseThrow();
        assertThat(updatedFreebie.getQuantity()).isEqualTo(4L);

        // EventLog COMPLETE 기록 확인
        boolean eventLogExists = eventLogRepository.findAll().stream()
                .anyMatch(log -> log.getOrderId().equals(order.getId())
                        && log.getMemberId().equals(member.getId())
                        && log.getStatus() == EventLog.EventLogStatus.COMPLETE);
        assertThat(eventLogExists).isTrue();
    }

    @Test
    @DisplayName("결제 성공 - 이벤트 조건 미충족 시 사은품 미지급")
    void createPayment_eventConditionNotMet_noFreebie() {
        // given - 100,000원 이상 구매 이벤트, 50,000원 주문
        Member member = createMember();
        Freebie freebie = createFreebie(5L);

        Event event = createAmountBuyEvent(100000L);
        createFreebieReward(event.getId(), freebie.getId());

        Order order = createOrder(member.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        paymentFacade.createPayment(member.getId(),
                new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then - 사은품 재고 변화 없음
        Freebie unchanged = freebieRepository.findById(freebie.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(5L);
    }

    @Test
    @DisplayName("결제 성공 - 이벤트 쿠폰 발급 보상 및 EventLog 기록")
    void createPayment_withEventCouponReward_success() {
        // given
        Member member = createMember();

        // 이벤트 보상용 쿠폰 (100개 발급 가능)
        Coupon eventCoupon = createCoupon(5000L, 3000L, 100L);

        // 30,000원 이상 구매 시 쿠폰 발급 이벤트
        Event event = createAmountBuyEvent(30000L);
        createCouponReward(event.getId(), eventCoupon.getId());

        // 50,000원 주문
        Order order = createOrder(member.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        // 이벤트 쿠폰 발급 수량 증가 확인
        Coupon updatedCoupon = couponRepository.findById(eventCoupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1L);

        // MemberCoupon 발급 확인
        boolean memberCouponIssued = memberCouponRepository.findAll().stream()
                .anyMatch(mc -> mc.getMemberId().equals(member.getId())
                        && mc.getCouponId().equals(eventCoupon.getId()));
        assertThat(memberCouponIssued).isTrue();

        // EventLog COMPLETE 기록 확인
        boolean eventLogExists = eventLogRepository.findAll().stream()
                .anyMatch(log -> log.getOrderId().equals(order.getId())
                        && log.getMemberId().equals(member.getId())
                        && log.getStatus() == EventLog.EventLogStatus.COMPLETE);
        assertThat(eventLogExists).isTrue();
    }

    @Test
    @DisplayName("결제 성공 - 주문 쿠폰 할인 + 이벤트 사은품 보상 동시 적용")
    void createPayment_withOrderCouponAndEventFreebieReward_success() {
        // given
        Member member = createMember();

        // 주문 할인 쿠폰 (10,000원 이상 구매 시 5,000원 할인)
        Coupon orderCoupon = createCoupon(10000L, 5000L, 100L);
        MemberCoupon memberCoupon = issueMemberCoupon(member.getId(), orderCoupon.getId());

        // 사은품 이벤트 (30,000원 이상 구매 시 사은품 지급)
        Freebie freebie = createFreebie(10L);
        Event event = createAmountBuyEvent(30000L);
        createFreebieReward(event.getId(), freebie.getId());

        // 50,000원 주문 + 주문 쿠폰 적용
        Order order = createOrder(member.getId(), memberCoupon.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        // 쿠폰 할인 적용 확인
        Order completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getDiscountAmount()).isEqualTo(5000L);
        assertThat(completedOrder.getFinalAmount()).isEqualTo(45000L);

        // 쿠폰 USED 처리 확인
        assertThat(memberCouponRepository.findById(memberCoupon.getId()).orElseThrow().isAvailable()).isFalse();

        // 사은품 재고 차감 확인
        assertThat(freebieRepository.findById(freebie.getId()).orElseThrow().getQuantity()).isEqualTo(9L);

        // EventLog 기록 확인
        boolean eventLogExists = eventLogRepository.findAll().stream()
                .anyMatch(log -> log.getOrderId().equals(order.getId())
                        && log.getStatus() == EventLog.EventLogStatus.COMPLETE);
        assertThat(eventLogExists).isTrue();
    }

    @Test
    @DisplayName("결제 성공 - 주문 쿠폰 할인 + 이벤트 쿠폰 보상 동시 적용")
    void createPayment_withOrderCouponAndEventCouponReward_success() {
        // given
        Member member = createMember();

        // 주문 할인 쿠폰 (사전 발급)
        Coupon orderCoupon = createCoupon(10000L, 5000L, 100L);
        MemberCoupon memberCoupon = issueMemberCoupon(member.getId(), orderCoupon.getId());

        // 이벤트 보상 쿠폰 (발급 이벤트)
        Coupon rewardCoupon = createCoupon(5000L, 2000L, 100L);
        Event event = createAmountBuyEvent(30000L);
        createCouponReward(event.getId(), rewardCoupon.getId());

        // 50,000원 주문 + 주문 쿠폰 적용
        Order order = createOrder(member.getId(), memberCoupon.getId(), 50000L);
        addOrderDetail(order.getId(), commonProductId, "testWhiskey", 50000L, 1);

        // when
        PaymentCreateResponse response = paymentFacade.createPayment(
                member.getId(), new PaymentCreateRequest(order.getId(), 50000L, 0L));

        // then
        assertThat(response.payment_unique_id()).startsWith("PAY-");

        // 주문 쿠폰 할인 적용
        Order completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(completedOrder.getDiscountAmount()).isEqualTo(5000L);
        assertThat(memberCouponRepository.findById(memberCoupon.getId()).orElseThrow().isAvailable()).isFalse();

        // 이벤트 쿠폰 발급 확인
        Coupon updatedRewardCoupon = couponRepository.findById(rewardCoupon.getId()).orElseThrow();
        assertThat(updatedRewardCoupon.getIssuedQuantity()).isEqualTo(1L);

        boolean eventLogExists = eventLogRepository.findAll().stream()
                .anyMatch(log -> log.getOrderId().equals(order.getId()) && log.getStatus() == EventLog.EventLogStatus.COMPLETE);
        assertThat(eventLogExists).isTrue();
    }

    @Test
    @DisplayName("동시성 - 재고 1개 상품에 10명 동시 결제, 정확히 1명만 성공")
    void createPayment_concurrency_onlyOneSucceeds() throws InterruptedException {
        // given - 재고 1개 한정 상품
        Product limitedProduct = productRepository.save(
                Product.register("testWhiskeyLimit", 100000L, new BigDecimal("75.000"), 700, commonCategoryDetailId, 1L)
        );

        productIdList.add(limitedProduct.getId());

        int threadCount = 10;
        List<Long> concurrentmemberIdList = new ArrayList<>();
        List<Long> concurrentorderIdList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Member m = memberRepository.save(Member.create(
                    UUID.randomUUID() + "@test.com", "pwd", "USER" + i,
                    "99990101", UUID.randomUUID().toString().substring(0, 8)));
            concurrentmemberIdList.add(m.getId());
            memberIdList.add(m.getId());

            Order o = orderRepository.save(Order.create(
                    m.getId(), null,
                    UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                    0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2"));
            concurrentorderIdList.add(o.getId());
            orderIdList.add(o.getId());

            orderDetailRepository.save(OrderDetail.create(
                    o.getId(), limitedProduct.getId(), "testWhiskeyLimit", 100000L, 1));
        }

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final Long memberId = concurrentmemberIdList.get(i);
            final Long orderId = concurrentorderIdList.get(i);
            executorService.submit(() -> {
                try {
                    paymentFacade.createPayment(memberId,
                            new PaymentCreateRequest(orderId, 100000L, 0L));
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Product updated = productRepository.findById(limitedProduct.getId()).orElseThrow();
        System.out.println("성공 : " + successCount.get() + " / 실패 : " + failCount.get() + " / 잔여 재고 : " + updated.getQuantity());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(updated.getQuantity()).isEqualTo(0L);
    }

    @Test
    @DisplayName("동시성 - 재고 10개 상품에 10명 동시 결제, 모두 성공 (재고 정합성 확인)")
    void createPayment_concurrency_allSucceed() throws InterruptedException {
        // given
        int threadCount = 10;
        List<Long> concurrentmemberIdList = new ArrayList<>();
        List<Long> concurrentorderIdList = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Member m = memberRepository.save(Member.create(
                    UUID.randomUUID() + "@test.com"
                    , "pwd"
                    , "USER" + i
                    , "99990101", UUID.randomUUID().toString().substring(0, 8)));
            concurrentmemberIdList.add(m.getId());
            memberIdList.add(m.getId());

            Order order = orderRepository.save(Order.create(
                    m.getId()
                    , null
                    , UUID.randomUUID().toString().replace("-", "").substring(0, 20)
                    ,0L
                    , 50000L
                    , 0L
                    , 50000L
                    , "testAddress1"
                    , "testAddress2"));
            concurrentorderIdList.add(order.getId());
            orderIdList.add(order.getId());

            orderDetailRepository.save(OrderDetail.create(
                    order.getId(), commonProductId, "testWhiskey", 50000L, 1));
        }

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final Long memberId = concurrentmemberIdList.get(i);
            final Long orderId = concurrentorderIdList.get(i);
            executorService.submit(() -> {
                try {
                    paymentFacade.createPayment(memberId, new PaymentCreateRequest(orderId, 50000L, 0L));
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    System.out.println("error: " + e.getClass().getName() + " - " + e.getMessage());
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Product updated = productRepository.findById(commonProductId).orElseThrow();
        System.out.println("성공 : " + successCount.get() + " / 실패 : " + failCount.get() + " / 잔여 재고 : " + updated.getQuantity());

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(updated.getQuantity()).isEqualTo(0L);
    }
}
