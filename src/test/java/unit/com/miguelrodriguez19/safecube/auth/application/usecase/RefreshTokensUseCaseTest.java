package unit.com.miguelrodriguez19.safecube.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.auth.application.dto.IssuedTokensResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AccessTokenIssuer;
import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRecord;
import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import com.miguelrodriguez19.safecube.auth.application.usecase.RefreshTokensUseCase;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class RefreshTokensUseCaseTest {

  @Mock private AccessTokenIssuer accessTokenIssuer;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokensUseCase target;

  @Test
  void shouldRotateRefreshTokenAndIssueNewAccessToken() {
    final var accountId = UUID.randomUUID();
    final var tokenId = UUID.randomUUID();
    final var issuedAt = Instant.now();

    final var storedToken =
        new RefreshTokenRecord(tokenId, accountId, "old-hash", issuedAt.plusSeconds(600), null);

    when(refreshTokenRepository.findByTokenHash("old-hash")).thenReturn(Optional.of(storedToken));

    when(accessTokenIssuer.issue(accountId, issuedAt)).thenReturn("new-access-token");

    final var result =
        target.execute(
            "old-hash", "new-raw-refresh", "new-hash", issuedAt, issuedAt.plusSeconds(3600));

    assertThat(result).isInstanceOf(Result.Success.class);

    final var tokens = result.success().orElseThrow();
    assertThat(tokens).isInstanceOf(IssuedTokensResult.class);
    assertThat(tokens.accessToken()).isEqualTo("new-access-token");
    assertThat(tokens.refreshToken()).isEqualTo("new-raw-refresh");

    verify(refreshTokenRepository).revoke(tokenId, issuedAt);
    verify(refreshTokenRepository).save(any(), eq(accountId), eq("new-hash"), any(), eq(issuedAt));
  }

  @Test
  void shouldFail_whenRefreshTokenDoesNotExist() {
    when(refreshTokenRepository.findByTokenHash("missing")).thenReturn(Optional.empty());

    final var result =
        target.execute("missing", "raw", "hash", Instant.now(), Instant.now().plusSeconds(3600));

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(AuthError.InvalidCredentials.class);

    verifyNoInteractions(accessTokenIssuer);
  }
}
