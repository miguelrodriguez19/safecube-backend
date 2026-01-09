package com.miguelrodriguez19.safecube.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshTokenRepository
 *
 * <p>Application port for managing refresh tokens lifecycle.
 *
 * <p>This port abstracts persistence concerns and allows
 * revocation and lookup of refresh tokens without leaking
 * infrastructure details to the application layer.
 */
public interface RefreshTokenRepository {

  /**
   * Persists a new refresh token.
   *
   * @param tokenId unique identifier of the token
   * @param accountId owning account identifier
   * @param tokenHash hashed token value
   * @param expiresAt expiration instant
   * @param createdAt creation instant
   */
  void save(
      final UUID tokenId,
      final UUID accountId,
      final String tokenHash,
      final Instant expiresAt,
      final Instant createdAt);

  /**
   * Finds a refresh token by its hashed value.
   *
   * @param tokenHash hashed token value
   * @return optional refresh token data
   */
  Optional<RefreshTokenRecord> findByTokenHash(final String tokenHash);

  /**
   * Marks a refresh token as revoked.
   *
   * @param tokenId token identifier
   * @param revokedAt revocation instant
   */
  void revoke(final UUID tokenId, final Instant revokedAt);

  /**
   * Revokes all refresh tokens associated with an account.
   *
   * @param accountId account identifier
   * @param revokedAt revocation instant
   */
  void revokeAllByAccountId(final UUID accountId, final Instant revokedAt);
}
