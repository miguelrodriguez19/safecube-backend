package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import java.time.Instant;
import java.util.UUID;

/** Result of retrieving a SecureItem. */
public record GetSecureItemResult(
    UUID itemId,
    ItemTypeDto itemType,
    int schemaVersion,
    String displayHint,
    byte[] payload,
    long payloadVersion,
    long itemRevision,
    long changeSequence,
    Instant updatedAt,
    Instant deletedAt) {}
