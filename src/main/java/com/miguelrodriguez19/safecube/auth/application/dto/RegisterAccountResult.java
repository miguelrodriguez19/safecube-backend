package com.miguelrodriguez19.safecube.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * RegisterAccountResult
 *
 * <p>Result returned after a successful account registration.
 */
public record RegisterAccountResult(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID accountId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt) {}
