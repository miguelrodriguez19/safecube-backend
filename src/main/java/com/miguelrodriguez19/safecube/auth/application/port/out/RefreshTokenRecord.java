package com.miguelrodriguez19.safecube.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * RefreshTokenRecord
 *
 * <p>Immutable projection of a persisted refresh token
 * used by the application layer.
 */
public record RefreshTokenRecord(
    UUID tokenId,
    UUID accountId,
    String tokenHash,
    Instant expiresAt,
    Instant revokedAt
) {}
