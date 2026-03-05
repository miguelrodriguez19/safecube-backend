package com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import java.time.Instant;
import java.util.Set;

/**
 * ListSecureItemsFilter
 *
 * <p>Explicit filter object for listing SecureItems.
 */
public record ListSecureItemsFilter(
    Instant createdAfter,
    Instant updatedAfter,
    ItemTypeDto type,
    // TODO: currently unimplemented. Out of MVP scope
    Set<String> labels,
    boolean includeDeleted,
    Integer limit,
    Order order) {

  public enum Order {
    DISPLAY_NAME_ASC,
    DISPLAY_NAME_DESC,
    UPDATED_AT_ASC,
    UPDATED_AT_DESC
  }
}
