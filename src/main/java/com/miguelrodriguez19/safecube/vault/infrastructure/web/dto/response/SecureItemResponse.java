package com.miguelrodriguez19.safecube.vault.infrastructure.web.dto.response;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.UUID;

/**
 * SecureItemResponse
 *
 * <p>HTTP response representation of a SecureItem.
 */
public record SecureItemResponse(
    UUID itemId,
    ItemType itemType,
    Integer schemaVersion,
    String displayHint,
    byte[] payload,
    long payloadVersion,
    Instant updatedAt,
    Instant deletedAt
) {}
