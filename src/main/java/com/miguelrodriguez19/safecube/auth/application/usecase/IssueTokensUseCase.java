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
 * IssueTokensUseCase
 *
 * <p>Issues access and refresh tokens for an authenticated account.
 */
@Component
@RequiredArgsConstructor
public class IssueTokensUseCase {

  private final AccessTokenIssuer accessTokenIssuer;
  private final RefreshTokenRepository refreshTokenRepository;

  public Result<IssuedTokensResult, AuthError> execute(
      final UUID accountId,
      final String rawRefreshToken,
      final String refreshTokenHash,
      final Instant issuedAt,
      final Instant refreshTokenExpiresAt) {

    final var accessToken = accessTokenIssuer.issue(accountId, issuedAt);

    final var refreshTokenId = UUID.randomUUID();

    refreshTokenRepository.save(
        refreshTokenId, accountId, refreshTokenHash, refreshTokenExpiresAt, issuedAt);

    return Result.success(new IssuedTokensResult(accessToken, rawRefreshToken, issuedAt));
  }
}
