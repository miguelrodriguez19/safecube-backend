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
        // given
        final var rawPassword = "password123";

        // when
        final var hash = target.hash(rawPassword);

        // then
        assertThat(hash).isNotNull();
        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo(rawPassword);
    }

    @Test
    void shouldMatchPasswordAgainstHash() {
        // given
        final var rawPassword = "password123";
        final var hash = target.hash(rawPassword);

        // when
        final var matches = target.matches(rawPassword, hash);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchDifferentPasswordAgainstHash() {
        // given
        final var rawPassword = "password123";
        final var differentPassword = "password456";
        final var hash = target.hash(rawPassword);

        // when
        final var matches = target.matches(differentPassword, hash);

        // then
        assertThat(matches).isFalse();
    }
}
