package com.miguelrodriguez19.safecube.vault.application.dto.result;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.time.Instant;
import java.util.UUID;

/** Result of retrieving a SecureItem. */
public record GetSecureItemResult(
    UUID itemId,
    ItemType itemType,
    int schemaVersion,
    String displayHint,
    byte[] payload,
    long payloadVersion,
    Instant updatedAt,
    Instant deletedAt) {}
