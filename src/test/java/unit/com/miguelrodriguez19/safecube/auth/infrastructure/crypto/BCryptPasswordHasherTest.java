package unit.com.miguelrodriguez19.safecube.auth.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.crypto.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

/**
 * BCryptPasswordHasherTest
 *
 * <p>Unit tests for {@link BCryptPasswordHasher}.
 */
@UnitTest
class BCryptPasswordHasherTest {

  private final BCryptPasswordHasher target = new BCryptPasswordHasher();

  @Test
  void shouldHashPassword() {
    final var rawPassword = "password123";

    final var hash = target.hash(rawPassword);

    assertThat(hash).isNotNull().isNotBlank().isNotEqualTo(rawPassword);
  }

  @Test
  void shouldMatchPasswordAgainstHash() {
    final var rawPassword = "password123";
    final var hash = target.hash(rawPassword);

    final var matches = target.matches(rawPassword, hash);

    assertThat(matches).isTrue();
  }

  @Test
  void shouldNotMatchDifferentPasswordAgainstHash() {
    final var rawPassword = "password123";
    final var differentPassword = "password456";
    final var hash = target.hash(rawPassword);

    final var matches = target.matches(differentPassword, hash);

    assertThat(matches).isFalse();
  }
}
