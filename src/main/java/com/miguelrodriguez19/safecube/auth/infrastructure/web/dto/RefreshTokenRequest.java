package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * RefreshTokenRequest
 *
 * <p>HTTP request for refreshing access tokens.
 */
@Schema(description = "Request to refresh authentication tokens.")
public record RefreshTokenRequest(
    @Schema(
            description = "Refresh token previously issued by the server.",
            accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank String refreshToken) {}
