package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * SecureItemSummaryResponse
 *
 * <p>Lightweight representation of a SecureItem used in list responses.
 */
public record SecureItemSummaryResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID itemId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String itemType,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int schemaVersion,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String displayHint,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long payloadVersion,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt,
    @Schema(type = "string", format = "date-time", nullable = true) Instant deletedAt) {}
