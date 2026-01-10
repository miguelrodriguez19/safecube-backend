package com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RefreshTokenJpaEntity
 *
 * <p>JPA entity representing a persisted refresh token.
 *
 * <p>Refresh tokens are opaque, hashed before persistence, and have an independent lifecycle from
 * {@link AuthAccountJpaEntity}.
 */
@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenJpaEntity {

  @Id private UUID tokenId;

  /** Account identifier this refresh token belongs to. */
  @Column(nullable = false)
  private UUID accountId;

  /**
   * Hash of the refresh token value.
   *
   * <p>The raw token is never persisted.
   */
  @Column(nullable = false, unique = true)
  private String tokenHash;

  /** Token expiration instant. */
  @Column(nullable = false)
  private Instant expiresAt;

  /** Creation instant. */
  @Column(nullable = false)
  private Instant createdAt;

  /** Revocation instant, if the token has been revoked. */
  private Instant revokedAt;
}
