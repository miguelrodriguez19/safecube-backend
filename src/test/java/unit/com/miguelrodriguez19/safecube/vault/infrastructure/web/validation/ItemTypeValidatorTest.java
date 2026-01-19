package unit.com.miguelrodriguez19.safecube.vault.infrastructure.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.ItemTypeValidator;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class ItemTypeValidatorTest {

  private final ItemTypeValidator target = new ItemTypeValidator();

  @Test
  void shouldReturnTrue_givenValidItemType() {
    final var value = "PASSWORD";

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
