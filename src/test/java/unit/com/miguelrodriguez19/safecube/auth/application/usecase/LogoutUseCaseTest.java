package unit.com.miguelrodriguez19.safecube.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import com.miguelrodriguez19.safecube.auth.application.usecase.LogoutUseCase;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class LogoutUseCaseTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private LogoutUseCase target;

  @Test
  void shouldRevokeAllTokensForAccount() {
    final var accountId = UUID.randomUUID();
    final var revokedAt = Instant.now();

    assertDoesNotThrow(() -> target.execute(accountId, revokedAt));

    verify(refreshTokenRepository).revokeAllByAccountId(accountId, revokedAt);
  }
}
