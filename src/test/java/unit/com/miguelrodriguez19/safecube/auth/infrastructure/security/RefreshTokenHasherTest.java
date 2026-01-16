package unit.com.miguelrodriguez19.safecube.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

import com.miguelrodriguez19.safecube.auth.infrastructure.exception.CryptoHashingException;
import com.miguelrodriguez19.safecube.auth.infrastructure.security.RefreshTokenHasher;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import unit.annotation.UnitTest;

@UnitTest
class RefreshTokenHasherTest {

  @Test
  void shouldProduceSameHash_givenSameToken() {
    final var hasher = new RefreshTokenHasher("secret");
    final var hash1 = hasher.hash("token");
    final var hash2 = hasher.hash("token");

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void shouldProduceDifferentHashes_givenDifferentToken() {
    final var hasher = new RefreshTokenHasher("secret");

    assertThat(hasher.hash("a")).isNotEqualTo(hasher.hash("b"));
  }

  @Test
  void shouldThrowCryptoHashingException_whenMacAlgorithmIsUnavailable() {
    try (MockedStatic<Mac> macMock = mockStatic(Mac.class)) {
      final var algorithm = "HmacSHA256";
      macMock
          .when(() -> Mac.getInstance(algorithm))
          .thenThrow(new NoSuchAlgorithmException("boom"));

      final var hasher = new RefreshTokenHasher("secret");

      assertThatThrownBy(() -> hasher.hash("raw-token"))
          .isInstanceOf(CryptoHashingException.class)
          .hasMessageContaining("Failed to hash token using " + "HmacSHA256")
          .hasCauseInstanceOf(NoSuchAlgorithmException.class);
    }
  }
}
