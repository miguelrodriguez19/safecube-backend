package com.miguelrodriguez19.safecube.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * AuthTokensResponse
 *
 * <p>HTTP response containing issued access and refresh tokens.
 */
@Schema(description = "Issued authentication tokens.")
public record AuthTokensResponse(
    @Schema(
            description = "Access token for authenticated requests (JWT).",
            accessMode = Schema.AccessMode.READ_ONLY)
        String accessToken,
    @Schema(
            description = "Refresh token used to obtain new token pairs.",
            accessMode = Schema.AccessMode.READ_ONLY)
        String refreshToken,
    @Schema(
            description = "Instant when the token pair was issued.",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "date-time")
        Instant issuedAt) {}
