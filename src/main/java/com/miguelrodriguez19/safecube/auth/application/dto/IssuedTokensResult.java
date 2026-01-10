package com.miguelrodriguez19.safecube.auth.application.dto;

import java.time.Instant;

/**
 * IssuedTokensResult
 *
 * <p>Result containing newly issued access and refresh tokens.
 */
public record IssuedTokensResult(
    String accessToken,
    String refreshToken,
    Instant issuedAt
) {}
