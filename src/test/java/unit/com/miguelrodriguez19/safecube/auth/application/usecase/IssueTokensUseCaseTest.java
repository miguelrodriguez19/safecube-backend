package unit.com.miguelrodriguez19.safecube.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.auth.application.dto.IssuedTokensResult;
import com.miguelrodriguez19.safecube.auth.application.port.out.AccessTokenIssuer;
import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import com.miguelrodriguez19.safecube.auth.application.usecase.IssueTokensUseCase;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class IssueTokensUseCaseTest {

    @Mock private AccessTokenIssuer accessTokenIssuer;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private IssueTokensUseCase target;

    @Test
    void shouldIssueAccessAndRefreshTokens() {
        final var accountId = UUID.randomUUID();
        final var issuedAt = Instant.now();
        final var refreshTokenExpiresAt = issuedAt.plusSeconds(3600);

        final var rawRefreshToken = "raw-refresh-token";
        final var refreshTokenHash = "hashed-refresh-token";
        final var accessToken = "access-token";

        when(accessTokenIssuer.issue(accountId, issuedAt))
                .thenReturn(accessToken);

        final var result =
                target.execute(
                        accountId,
                        rawRefreshToken,
                        refreshTokenHash,
                        issuedAt,
                        refreshTokenExpiresAt);

        assertThat(result).isInstanceOf(Result.Success.class);

        final var tokens = result.success().orElseThrow();
        assertThat(tokens).isInstanceOf(IssuedTokensResult.class);
        assertThat(tokens.accessToken()).isEqualTo(accessToken);
        assertThat(tokens.refreshToken()).isEqualTo(rawRefreshToken);
        assertThat(tokens.issuedAt()).isEqualTo(issuedAt);

        verify(refreshTokenRepository)
                .save(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(accountId),
                        org.mockito.ArgumentMatchers.eq(refreshTokenHash),
                        org.mockito.ArgumentMatchers.eq(refreshTokenExpiresAt),
                        org.mockito.ArgumentMatchers.eq(issuedAt));
    }
}
