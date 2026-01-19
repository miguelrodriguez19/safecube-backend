package unit.com.miguelrodriguez19.safecube.vault.infrastructure.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.OrderValidator;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class OrderValidatorTest {

    private final OrderValidator target = new OrderValidator();

    @Test
    void shouldReturnTrue_givenValidItemType() {
        final var value = "DISPLAY_NAME_DESC";

        final var result = target.isValid(value, null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_givenInvalidItemType() {
        final var value = "INVALID_TYPE";

        final var result = target.isValid(value, null);

        assertThat(result).isFalse();
    }
}
