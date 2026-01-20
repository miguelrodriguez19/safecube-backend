package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.secureitem.response;

import java.time.Instant;
import java.util.UUID;

/**
 * SecureItemSummaryResponse
 *
 * <p>Lightweight representation of a SecureItem used in list responses.
 */
public record SecureItemSummaryResponse(
    UUID itemId,
    String itemType,
    int schemaVersion,
    String displayHint,
    long payloadVersion,
    Instant updatedAt,
    Instant deletedAt) {}
