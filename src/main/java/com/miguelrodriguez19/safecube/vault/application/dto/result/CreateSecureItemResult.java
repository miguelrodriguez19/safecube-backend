package com.miguelrodriguez19.safecube.vault.application.dto.result;

import java.time.Instant;
import java.util.UUID;

/** Result of a successful SecureItem creation. */
public record CreateSecureItemResult(UUID itemId, Instant createdAt) {}
