package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Result of a successful SecureItem deletion. */
public record DeleteSecureItemResult(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID itemId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID mutationId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long payloadVersion,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long itemRevision,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long changeSequence,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant deletedAt) {}
