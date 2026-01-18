package com.miguelrodriguez19.safecube.vault.application.dto.query;

import java.util.UUID;

/**
 * Query to retrieve a SecureItem by id.
 */
public record GetSecureItemQuery(
        UUID accountId,
        UUID itemId
) {
}
