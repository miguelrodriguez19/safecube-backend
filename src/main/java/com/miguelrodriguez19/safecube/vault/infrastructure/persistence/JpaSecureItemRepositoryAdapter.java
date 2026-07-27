package com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.SecureItemMapper;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.specification.SecureItemSpecifications;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/**
 * JpaSecureItemRepositoryAdapter
 *
 * <p>JPA-backed implementation of {@link SecureItemRepository}. Executes filtering, ordering and
 * limiting at query level.
 */
@Repository
@RequiredArgsConstructor
public class JpaSecureItemRepositoryAdapter implements SecureItemRepository {

  private final SecureItemJpaRepository jpaRepository;
  private final SecureItemMapper mapper;

  @Override
  public List<SecureItem> findFilteredByAccount(
      final UUID accountId, final ListSecureItemsFilter filter) {

    var spec = Specification.where(SecureItemSpecifications.accountIs(accountId));

    if (filter.createdAfter() != null) {
      spec = spec.and(SecureItemSpecifications.createdAfter(filter.createdAfter()));
    }

    if (filter.updatedAfter() != null) {
      spec = spec.and(SecureItemSpecifications.updatedAfter(filter.updatedAfter()));
    }

    if (filter.type() != null) {
      spec = spec.and(SecureItemSpecifications.hasType(filter.type().name()));
    }

    if (!filter.includeDeleted()) {
      spec = spec.and(SecureItemSpecifications.notDeleted());
    }

    final var pageable = PageRequest.of(0, resolveLimit(filter), resolveSort(filter));

    return jpaRepository.findAll(spec, pageable).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void save(final SecureItem secureItem) {
    jpaRepository.save(mapper.toEntity(secureItem));
  }

  @Override
  public SecureItem findByIdAndAccount(final UUID itemId, final UUID accountId) {
    final var entity = jpaRepository.findByItemIdAndAccountId(itemId, accountId).orElse(null);

    return entity == null ? null : mapper.toDomain(entity);
  }

  @Override
  public List<SecureItem> findChanges(final UUID accountId, final long after, final int limit) {
    return jpaRepository
        .findByAccountIdAndChangeSequenceGreaterThanOrderByChangeSequenceAsc(
            accountId, after, PageRequest.of(0, limit))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public long nextChangeSequence(final UUID accountId) {
    return jpaRepository.nextChangeSequence(accountId);
  }

  @Override
  public boolean updateIfRevisionMatches(
      final SecureItem secureItem, final long expectedItemRevision) {
    return jpaRepository.updateIfRevisionMatches(
            secureItem.getItemId(),
            secureItem.getAccountId(),
            expectedItemRevision,
            secureItem.getItemRevision(),
            secureItem.getChangeSequence(),
            secureItem.getItemType().name(),
            secureItem.getSchemaVersion(),
            secureItem.getDisplayHint(),
            secureItem.getPayload(),
            secureItem.getPayloadVersion(),
            secureItem.getUpdatedAt())
        == 1;
  }

  @Override
  public boolean softDeleteIfRevisionMatches(
      final UUID itemId,
      final UUID accountId,
      final long expectedItemRevision,
      final long nextItemRevision,
      final long changeSequence,
      final Instant deletedAt) {
    return jpaRepository.softDeleteIfRevisionMatches(
            itemId, accountId, expectedItemRevision, nextItemRevision, changeSequence, deletedAt)
        == 1;
  }

  private int resolveLimit(final ListSecureItemsFilter filter) {
    return filter.limit() != null ? filter.limit() : 100;
  }

  private Sort resolveSort(final ListSecureItemsFilter filter) {
    return switch (filter.order()) {
      case DISPLAY_NAME_ASC -> Sort.by("displayHint").ascending();
      case DISPLAY_NAME_DESC -> Sort.by("displayHint").descending();
      case UPDATED_AT_ASC -> Sort.by("updatedAt").ascending();
      case UPDATED_AT_DESC -> Sort.by("updatedAt").descending();
    };
  }
}
