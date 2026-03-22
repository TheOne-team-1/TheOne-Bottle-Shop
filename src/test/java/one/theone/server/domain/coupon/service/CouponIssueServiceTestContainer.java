package one.theone.server.domain.coupon.service;

import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
import one.theone.server.common.RedisTestContainer;

public class CouponIssueServiceTestContainer extends RedisTestContainer {



    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    private Long couponId;
    private final List<Long> memberIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 발급 수량 100개 쿠폰 생성
        Coupon coupon = couponRepository.save(Coupon.register(
                "TEST_COUPON"
                , Coupon.CouponUseType.AMOUNT
                , 10000L
                , 5000L
                , 100L
                , LocalDateTime.now().minusDays(1)
                , LocalDate.now().plusDays(7)
        ));

        couponId = coupon.getId();

        // 100명 회원 생성
        for (int i = 0; i < 100; i++) {
            String uniqueEmail = UUID.randomUUID() + "@test.com";
            Member member = memberRepository.save(
                    Member.create(uniqueEmail, "pwd", "TEST" + i, "99990101", UUID.randomUUID().toString().substring(0, 8))
            );
            memberIds.add(member.getId());
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 발급된 MemberCoupon 삭제
        memberCouponRepository.findAll().stream()
                .filter(memberCoupon -> memberIds.contains(memberCoupon.getMemberId()))
                .forEach(memberCoupon -> memberCouponRepository.delete(memberCoupon));

        couponRepository.deleteById(couponId);
        memberRepository.deleteAllById(memberIds);

        memberIds.clear();
    }

    @Test
    @DisplayName("NO_LOCK")
    void withoutLock() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    couponService.issueCouponByEvent(couponId, memberId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        System.out.println("락 없는 최종 발급 수량 : " + coupon.getIssuedQuantity());

        // 발급 수량이 100 이 아님
        assertThat(coupon.getIssuedQuantity()).isNotEqualTo(100L);
    }

    @Test
    @DisplayName("WithRedisLock")
    void withSpinLock() throws InterruptedException {
        int threadCount = 100;

        AtomicInteger failCount = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    couponIssueService.issueCouponWithLock(couponId, memberId, 1L);
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        System.out.println("레디스 락 최종 발급 수량 : " + coupon.getIssuedQuantity());

        // 발급에 대한 정합성으로 체크
        assertThat(coupon.getAvailQuantity()).isEqualTo(failCount.get() + coupon.getIssuedQuantity());
    }
}
