package com.miguelrodriguez19.safecube.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * UserProfileResponse
 *
 * <p>HTTP response representation of a user profile.
 */
public record UserProfileResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID userId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID accountId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String displayName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt) {}
