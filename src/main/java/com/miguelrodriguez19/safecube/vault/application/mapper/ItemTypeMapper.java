package com.miguelrodriguez19.safecube.vault.application.mapper;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import org.springframework.stereotype.Component;

/**
 * ItemTypeMapper
 *
 * <p>Maps between the domain {@link ItemType} and the application-level {@link ItemTypeDto}.
 */
@Component
public class ItemTypeMapper {

  public ItemTypeDto toDto(final ItemType itemType) {
    return ItemTypeDto.valueOf(itemType.name());
  }

  public ItemType toDomain(final ItemTypeDto itemTypeDto) {
    return ItemType.valueOf(itemTypeDto.name());
  }
}
