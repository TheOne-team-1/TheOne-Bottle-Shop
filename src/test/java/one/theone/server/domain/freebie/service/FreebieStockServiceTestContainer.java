package one.theone.server.domain.freebie.service;

import one.theone.server.domain.freebie.entity.Freebie;
import one.theone.server.domain.freebie.repository.FreebieRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import one.theone.server.common.RedisTestContainer;


public class FreebieStockServiceTestContainer extends RedisTestContainer {



    @Autowired
    private FreebieService freebieService;

    @Autowired
    private FreebieStockService freebieStockService;

    @Autowired
    private FreebieRepository freebieRepository;

    private Long freebieId;

    @BeforeEach
    void beforeSetUp() {
        // 재고 100개 상품 생성
        Freebie freebie = freebieRepository.save(Freebie.register(1L, "test",100L));
        freebieId = freebie.getId();
    }

    @AfterEach
    void tearDown() {
        freebieRepository.deleteById(freebieId);
    }

    @Test
    @DisplayName("NO_LOCK")
    void withoutLock() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 락 없는 재고 감소
                    freebieService.decreaseStock(freebieId, 1L);
                } finally {
                    latch.countDown(); // 스레드 완료 카운트 감소
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        Freebie freebie = freebieRepository.findById(freebieId).orElseThrow();

        // 동시성 문제로 재고가 0이 아님
        assertThat(freebie.getQuantity()).isNotEqualTo(0);
        System.out.println("락 없는 최종 재고 : " + freebie.getQuantity());
    }

    @Test
    @DisplayName("WithRedisLock - decreaseStock")
    void withSpinLock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    freebieStockService.decreaseStockWithLock(freebieId, 1L);
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                        latch.countDown();
                    }
                });
        }

        latch.await();
        executorService.shutdown();

        Freebie freebie = freebieRepository.findById(freebieId).orElseThrow();

        System.out.println("재고 감소 - 최종 재고 : " + freebie.getQuantity());
        assertThat(freebie.getQuantity()).isEqualTo(failCount.get());
    }

    @Test
    @DisplayName("WithRedisLock - increaseStock")
    void withSpinLock_increaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    freebieStockService.increaseStockWithLock(freebieId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Freebie freebie = freebieRepository.findById(freebieId).orElseThrow();

        System.out.println("재고 증가 - 최종 재고 : " + freebie.getQuantity());
        assertThat(freebie.getQuantity()).isEqualTo(200L - failCount.get());
    }
}
