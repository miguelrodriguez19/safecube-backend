package com.miguelrodriguez19.safecube.auth.application.usecase;

import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * LogoutUseCase
 *
 * <p>Revokes all refresh tokens associated with an account.
 */
@Component
@RequiredArgsConstructor
public class LogoutUseCase {

  private final RefreshTokenRepository refreshTokenRepository;

  public void execute(
          final UUID accountId,
          final Instant revokedAt) {

    refreshTokenRepository.revokeAllByAccountId(accountId, revokedAt);
  }
}
