package one.theone.server.domain.cart.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CartRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("CartAddRequest 검증 실패")
    void cartAddRequest_invalid() {
        CartAddRequest request = new CartAddRequest(0L, 0);

        Set<ConstraintViolation<CartAddRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    @DisplayName("CartAddRequest 검증 성공")
    void cartAddRequest_valid() {
        CartAddRequest request = new CartAddRequest(1L, 1);

        Set<ConstraintViolation<CartAddRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CartUpdateQuantityRequest 검증 실패")
    void cartUpdateQuantityRequest_invalid() {
        CartUpdateQuantityRequest request = new CartUpdateQuantityRequest(0);

        Set<ConstraintViolation<CartUpdateQuantityRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("CartUpdateQuantityRequest 검증 성공")
    void cartUpdateQuantityRequest_valid() {
        CartUpdateQuantityRequest request = new CartUpdateQuantityRequest(2);

        Set<ConstraintViolation<CartUpdateQuantityRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}