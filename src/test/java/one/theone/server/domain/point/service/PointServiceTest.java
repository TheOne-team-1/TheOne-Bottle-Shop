package one.theone.server.domain.point.service;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.entity.PointUseDetail;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import one.theone.server.domain.point.repository.PointUseDetailRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock private PointRepository pointRepository;
    @Mock private PointLogRepository pointLogRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PointUseDetailRepository pointUseDetailRepository;
    @Mock private MemberRepository memberRepository;

    @Mock private Order order;

    @Test
    @DisplayName("포인트 증가 조정 성공")
    void adjustPoint_increase_success() {
        // given
        Long memberId = 1L;
        Long amount = 1000L;

        Member member = Member.create("test@test.com", "password", "테스트", "20000101", "ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(0L);

        Point point = Point.register(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.adjustPoint(memberId, new PointAdjustRequest(amount, "테스트 지급"));

        // then
        assertThat(point.getBalance()).isEqualTo(1000L);
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("포인트 감소 조정 실패 - 잔액 부족")
    void adjustPoint_decrease_fail_insufficient() {
        // given
        Long memberId = 1L;

        Member member = Member.create("test@test.com", "password", "테스트", "20000101", "ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(500L);

        // when & then
        assertThatThrownBy(() ->
                pointService.adjustPoint(memberId, new PointAdjustRequest(-1000L, "테스트 차감"))
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("포인트 조정 실패 - 회원 없음")
    void adjustPoint_memberNotFound() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.adjustPoint(999L, new PointAdjustRequest(1000L, "테스트")))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("포인트 내역 조회 성공")
    void getPointLogs_success() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        PointLogsGetRequest request = new PointLogsGetRequest(null, null, null);
        given(pointLogRepository.findPointLogs(memberId, request, pageable))
                .willReturn(new PageImpl<>(List.of()));

        // when
        PageResponse<PointLogsGetResponse> result = pointService.getPointLogs(memberId, request, 0, 10);

        // then
        assertThat(result.content()).isEmpty();
    }


    @Test
    @DisplayName("포인트 사용 성공")
    void usePoint_success() {
        // given
        Long memberId = 1L;
        Long orderId = 10L;

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getUsedPoint()).willReturn(500L);

        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(1000L);

        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(1000L, "테스트 지급"), 1000L);
        given(pointLogRepository.findAvailablePoints(memberId)).willReturn(List.of(earnLog));

        Point point = Point.register(memberId);
        point.updateBalance(1000L);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.usePoint(memberId, orderId);

        // then
        assertThat(point.getBalance()).isEqualTo(500L);
        assertThat(earnLog.getRemainingAmount()).isEqualTo(500L);
        verify(pointUseDetailRepository).save(any(PointUseDetail.class));
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void usePoint_insufficient() {
        // given
        Long memberId = 1L;
        Long orderId = 10L;

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getUsedPoint()).willReturn(2000L);
        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(1000L);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(memberId, orderId))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 주문 없음")
    void usePoint_orderNotFound() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(1L, 999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("포인트 환불 성공")
    void refundPoint_success() {
        // given
        Long memberId = 1L;
        Long orderId = 10L;
        Long usedAmount = 500L;

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getUsedPoint()).willReturn(usedAmount);
        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(500L);

        PointUseDetail useDetail = PointUseDetail.register(100L, orderId, usedAmount);
        given(pointUseDetailRepository.findByOrderId(orderId)).willReturn(List.of(useDetail));

        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(1000L, "테스트 지급"), 1000L);
        earnLog.deduct(500L);
        given(pointLogRepository.findById(100L)).willReturn(Optional.of(earnLog));

        Point point = Point.register(memberId);
        point.updateBalance(500L);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.refundPoint(memberId, orderId);

        // then
        assertThat(point.getBalance()).isEqualTo(1000L);
        assertThat(earnLog.getRemainingAmount()).isEqualTo(1000L);
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("포인트 환불 실패 - 주문 없음")
    void refundPoint_orderNotFound() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.refundPoint(1L, 999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("포인트 환불 실패 - 포인트 로그 없음")
    void refundPoint_pointLogNotFound() {
        // given
        Long memberId = 1L;
        Long orderId = 10L;

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getUsedPoint()).willReturn(500L);
        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(500L);

        PointUseDetail useDetail = PointUseDetail.register(999L, orderId, 500L); // 없는 pointLogId
        given(pointUseDetailRepository.findByOrderId(orderId)).willReturn(List.of(useDetail));
        given(pointLogRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.refundPoint(memberId, orderId))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("포인트 로그를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void earnPoint_success() {
        // given
        Long memberId = 1L;
        Long orderId = 10L;
        Long finalAmount = 10000L;

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Member member = Member.create("test@test.com", "password", "테스트", "20000101", "ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(0L);

        Point point = Point.register(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.earnPoint(memberId, orderId, finalAmount);

        // then
        assertThat(point.getBalance()).isEqualTo(100L); // 10000 * 1% = 100
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("포인트 적립 실패 - 회원 없음")
    void earnPoint_memberNotFound() {
        // given
        Long orderId = 10L;
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.earnPoint(999L, orderId, 10000L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("포인트 적립 실패 - 주문 없음")
    void earnPoint_orderNotFound() {
        // given
        Long memberId = 1L;

        Member member = Member.create("test@test.com", "password", "테스트", "20000101", "ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.earnPoint(1L, 999L, 10000L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("만료 포인트 소멸 처리 성공")
    void expirePoint_success() {
        // given
        Long memberId = 1L;

        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(1000L, "테스트 지급"), 1000L);
        given(pointLogRepository.findExpiredPoints()).willReturn(List.of(earnLog));
        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(1000L);

        Point point = Point.register(memberId);
        point.updateBalance(1000L);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.expirePoint();

        // then
        assertThat(point.getBalance()).isEqualTo(0L);
        assertThat(earnLog.getRemainingAmount()).isEqualTo(0L);
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("이벤트 포인트 적립 성공")
    void earnEventPoint_success() {
        // given
        Long memberId = 1L;
        Long amount = 1000L;
        String description = "추천인 보상";

        Member member = Member.create("test@test.com", "password", "테스트", "20000101", "ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(pointLogRepository.sumAmountByMemberId(memberId)).willReturn(0L);

        Point point = Point.register(memberId);
        given(pointRepository.findByMemberId(memberId)).willReturn(Optional.of(point));
        given(pointLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        pointService.earnEventPoint(memberId, amount, description);

        // then
        assertThat(point.getBalance()).isEqualTo(1000L);
        verify(pointLogRepository).save(any(PointLog.class));
    }

    @Test
    @DisplayName("이벤트 포인트 적립 실패 - 회원 없음")
    void earnEventPoint_memberNotFound() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.earnEventPoint(999L, 1000L, "추천인 보상"))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }
}