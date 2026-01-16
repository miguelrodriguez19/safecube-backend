package unit.com.miguelrodriguez19.safecube.shared.result;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.shared.result.Result;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultTest {
  @Nested
  class Success {
    private final Result<String, Exception> target = Result.success("test");

    @Test
    void shouldReturnTrue_whenIsSuccess() {
      assertThat(target.isSuccess()).isTrue();
    }

    @Test
    void shouldReturnFalse_whenIsFailure() {
      assertThat(target.isFailure()).isFalse();
    }

    @Test
    void shouldReturnString_whenSuccessIsCalled() {
      assertThat(target.isSuccess()).isTrue();
      assertThat(target.success()).isPresent().get().isEqualTo("test");
    }

    @Test
    void shouldReturnEmpty_whenErrorIsCalled() {
      assertThat(target.isSuccess()).isTrue();
      assertThat(target.error()).isEmpty();
    }
  }

  @Nested
  class Failure {
    private final Result<String, Exception> target = Result.failure(new RuntimeException("test"));

    @Test
    void shouldReturnTrue_whenIsFailure() {
      assertThat(target.isFailure()).isTrue();
    }

    @Test
    void shouldReturnFalse_whenIsSuccess() {
      assertThat(target.isSuccess()).isFalse();
    }

    @Test
    void shouldReturnException_whenErrorIsCalled() {
      assertThat(target.isFailure()).isTrue();
      assertThat(target.error()).isPresent().get().isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnEmpty_whenSuccessIsCalled() {
      assertThat(target.isFailure()).isTrue();
      assertThat(target.success()).isEmpty();
    }
  }
}
