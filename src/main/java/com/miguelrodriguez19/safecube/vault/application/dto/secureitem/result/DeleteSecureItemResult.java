package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result;

import java.time.Instant;
import java.util.UUID;

/** Result of a successful SecureItem deletion. */
public record DeleteSecureItemResult(UUID itemId, Instant deletedAt) {}
