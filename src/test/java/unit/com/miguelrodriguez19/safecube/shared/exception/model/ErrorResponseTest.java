package unit.com.miguelrodriguez19.safecube.shared.exception.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.shared.exception.model.ErrorResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class ErrorResponseTest {

  @Test
  void simple_shouldCreateErrorResponseWithoutFields() {
    final var error = "SOME_ERROR";

    final var response = ErrorResponse.simple(error);

    assertThat(response.error()).isEqualTo(error);
    assertThat(response.fields()).isNull();
  }

  @Test
  void withFields_shouldCreateErrorResponseWithFields() {
    final var error = "VALIDATION_FAILED";
    final var fields = Map.of("email", "must not be blank");

    final var response = ErrorResponse.withFields(error, fields);

    assertThat(response.error()).isEqualTo(error);
    assertThat(response.fields()).containsEntry("email", "must not be blank");
  }
}
