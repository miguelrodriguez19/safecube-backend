package com.miguelrodriguez19.safecube.vault.application.dto.query;

import java.time.Instant;
import java.util.UUID;

/**
 * Query to list SecureItems for an account.
 */
public record ListSecureItemsQuery(
        UUID accountId,
        Instant since,
        boolean includeDeleted
) {
}
