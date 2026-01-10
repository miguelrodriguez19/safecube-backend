package unit.com.miguelrodriguez19.safecube.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.RefreshTokenHasher;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class RefreshTokenHasherTest {

  @Test
  void sameToken_shouldProduceSameHash() {
    final var hasher = new RefreshTokenHasher("secret");
    final var hash1 = hasher.hash("token");
    final var hash2 = hasher.hash("token");

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void differentTokens_shouldProduceDifferentHashes() {
    final var hasher = new RefreshTokenHasher("secret");

    assertThat(hasher.hash("a")).isNotEqualTo(hasher.hash("b"));
  }
}
