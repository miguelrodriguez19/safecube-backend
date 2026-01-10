package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import java.time.Instant;

/**
 * AuthTokensResponse
 *
 * <p>HTTP response containing issued access and refresh tokens.
 */
public record AuthTokensResponse(String accessToken, String refreshToken, Instant issuedAt) {}
