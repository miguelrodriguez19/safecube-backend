package com.miguelrodriguez19.safecube.vault.infrastructure.web.mapper;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter.Order;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * ListSecureItemsFilterMapper
 *
 * <p>Maps HTTP request parameters into a ListSecureItemsFilter.
 */
@Component
public class ListSecureItemsFilterMapper {

  public ListSecureItemsFilter from(
      final Instant createdAfter,
      final Instant updatedAfter,
      final String type,
      final Set<String> labels,
      final boolean includeDeleted,
      final Integer limit,
      final String order) {

    return new ListSecureItemsFilter(
        createdAfter,
        updatedAfter,
        type != null ? ItemTypeDto.valueOf(type) : null,
        labels,
        includeDeleted,
        limit,
        order != null ? Order.valueOf(order) : null);
  }
}
