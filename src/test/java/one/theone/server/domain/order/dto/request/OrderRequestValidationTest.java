package one.theone.server.domain.order.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("OrderCreateDirectRequest 검증 실패")
    void orderCreateDirectRequest_invalid() {
        OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                0L,
                0,
                null,
                -1L,
                "",
                ""
        );

        Set<ConstraintViolation<OrderCreateDirectRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("OrderCreateDirectRequest 검증 성공")
    void orderCreateDirectRequest_valid() {
        OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                1L,
                1,
                null,
                0L,
                "서울시",
                "101동"
        );

        Set<ConstraintViolation<OrderCreateDirectRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("OrderCreateFromCartRequest 검증 실패")
    void orderCreateFromCartRequest_invalid() {
        OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                null,
                -1L,
                "",
                ""
        );

        Set<ConstraintViolation<OrderCreateFromCartRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("OrderCreateFromCartRequest 검증 성공")
    void orderCreateFromCartRequest_valid() {
        OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                null,
                0L,
                "서울시",
                "101동"
        );

        Set<ConstraintViolation<OrderCreateFromCartRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}