package com.miguelrodriguez19.safecube.auth.application.usecase;

import com.miguelrodriguez19.safecube.auth.application.dto.IssuedTokensResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AccessTokenIssuer;
import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * RefreshTokensUseCase
 *
 * <p>Issues a new access token using a valid refresh token.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokensUseCase {

  private final AccessTokenIssuer accessTokenIssuer;
  private final RefreshTokenRepository refreshTokenRepository;

  public Result<IssuedTokensResult, AuthError> execute(
      final String refreshTokenHash,
      final String newRawRefreshToken,
      final String newRefreshTokenHash,
      final Instant issuedAt,
      final Instant newRefreshTokenExpiresAt) {

    final var storedToken =
        refreshTokenRepository.findByTokenHash(refreshTokenHash)
            .orElse(null);

    if (storedToken == null) {
      return Result.failure(new AuthError.InvalidCredentials());
    }

    if (storedToken.revokedAt() != null
        || storedToken.expiresAt().isBefore(issuedAt)) {
      return Result.failure(new AuthError.InvalidCredentials());
    }

    refreshTokenRepository.revoke(
        storedToken.tokenId(),
        issuedAt
    );

    final var accessToken =
        accessTokenIssuer.issue(storedToken.accountId(), issuedAt);

    refreshTokenRepository.save(
        UUID.randomUUID(),
        storedToken.accountId(),
        newRefreshTokenHash,
        newRefreshTokenExpiresAt,
        issuedAt
    );

    return Result.success(
        new IssuedTokensResult(accessToken, newRawRefreshToken, issuedAt)
    );
  }
}
