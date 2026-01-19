package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper;

import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaEntity;
import org.springframework.stereotype.Component;

/**
 * SecureItemMapper
 *
 * <p>Maps between {@link SecureItem} domain model and {@link SecureItemJpaEntity}.
 */
@Component
public class SecureItemMapper {

  public SecureItemJpaEntity toEntity(final SecureItem domain) {
    return new SecureItemJpaEntity(
        domain.getItemId(),
        domain.getAccountId(),
        domain.getItemType().name(),
        domain.getSchemaVersion(),
        domain.getDisplayHint(),
        domain.getPayload(),
        domain.getPayloadVersion(),
        domain.getCreatedAt(),
        domain.getUpdatedAt(),
        domain.getDeletedAt());
  }

  public SecureItem toDomain(final SecureItemJpaEntity entity) {
    return SecureItem.restore(
        entity.getItemId(),
        entity.getAccountId(),
        ItemType.valueOf(entity.getItemType()),
        entity.getSchemaVersion(),
        entity.getDisplayHint(),
        entity.getPayload(),
        entity.getPayloadVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt());
  }
}
