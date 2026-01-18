package com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.SecureItemMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * JpaSecureItemRepositoryAdapter
 *
 * <p>JPA-backed implementation of {@link SecureItemRepository}.
 */
@Repository
@RequiredArgsConstructor
public class JpaSecureItemRepositoryAdapter implements SecureItemRepository {

  private final SecureItemJpaRepository jpaRepository;
  private final SecureItemMapper mapper;

  @Override
  public void save(final SecureItem secureItem) {
    final var entity = mapper.toEntity(secureItem);
    jpaRepository.save(entity);
  }

  @Override
  public SecureItem findByIdAndAccount(final UUID itemId, final UUID accountId) {
    final var entity = jpaRepository.findByItemIdAndAccountId(itemId, accountId).orElse(null);
    if (entity == null) {
      return null;
    }
    return mapper.toDomain(entity);
  }

  @Override
  public List<SecureItem> findByAccount(final UUID accountId) {
    return jpaRepository.findAllByAccountId(accountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void update(final SecureItem secureItem) {
    final var entity = mapper.toEntity(secureItem);
    jpaRepository.save(entity);
  }

  @Override
  public void softDelete(final UUID itemId, final UUID accountId, final Instant deletedAt) {
    jpaRepository.softDelete(itemId, accountId, deletedAt);
  }
}
