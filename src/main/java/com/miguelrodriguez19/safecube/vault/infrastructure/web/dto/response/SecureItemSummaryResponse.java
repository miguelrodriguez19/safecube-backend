package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.UUID;

/**
 * SecureItemSummaryResponse
 *
 * <p>Lightweight representation of a SecureItem used in list responses.
 */
public record SecureItemSummaryResponse(
    UUID itemId,
    ItemType itemType,
    int schemaVersion,
    String displayHint,
    long payloadVersion,
    Instant updatedAt,
    Instant deletedAt
) {}
