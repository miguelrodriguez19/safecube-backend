package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * RefreshTokenRequest
 *
 * <p>HTTP request for refreshing access tokens.
 */
public record RefreshTokenRequest(
    @NotBlank String refreshToken
) {}
