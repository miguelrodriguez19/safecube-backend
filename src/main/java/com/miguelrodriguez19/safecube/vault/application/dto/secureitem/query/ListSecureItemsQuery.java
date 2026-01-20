package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query;

import java.util.UUID;

/** Query to list SecureItems for an account. */
public record ListSecureItemsQuery(UUID accountId, ListSecureItemsFilter filter) {}
