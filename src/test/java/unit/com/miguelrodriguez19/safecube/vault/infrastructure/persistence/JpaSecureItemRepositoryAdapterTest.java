package unit.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaSecureItemRepositoryAdapter;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaEntity;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.SecureItemMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import unit.annotation.UnitTest;

@UnitTest
class JpaSecureItemRepositoryAdapterTest {

  @Mock SecureItemJpaRepository jpaRepository;
  @Mock SecureItemMapper mapper;

  @InjectMocks private JpaSecureItemRepositoryAdapter target;

  @Test
  void shouldSaveSecureItem() {
    final var now = Instant.now();

    final var domainItem =
        SecureItem.of(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload".getBytes(),
            now);

    final var entity =
        new SecureItemJpaEntity(
            domainItem.getItemId(),
            domainItem.getAccountId(),
            domainItem.getItemType().name(),
            domainItem.getSchemaVersion(),
            domainItem.getDisplayHint(),
            domainItem.getPayload(),
            domainItem.getPayloadVersion(),
            domainItem.getCreatedAt(),
            domainItem.getUpdatedAt(),
            null);

    when(mapper.toEntity(domainItem)).thenReturn(entity);

    target.save(domainItem);

    verify(mapper).toEntity(domainItem);
    verify(jpaRepository).save(entity);
  }

  @Test
  void shouldFindSecureItemByIdAndAccount() {
    final var itemId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();

    final var entity =
        new SecureItemJpaEntity(
            itemId,
            accountId,
            ItemType.NOTE.name(),
            1,
            "Note",
            "payload".getBytes(),
            1L,
            Instant.now(),
            Instant.now(),
            null);

    final var domainItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "Note",
            entity.getPayload(),
            1L,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            null);

    when(jpaRepository.findByItemIdAndAccountId(itemId, accountId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domainItem);

    final var result = target.findByIdAndAccount(itemId, accountId);

    assertThat(result).isNotNull();
    assertThat(result.getItemId()).isEqualTo(itemId);

    verify(jpaRepository).findByItemIdAndAccountId(itemId, accountId);
    verify(mapper).toDomain(entity);
  }

  @Test
  void shouldReturnNull_whenSecureItemNotFound() {
    final var itemId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findByItemIdAndAccountId(itemId, accountId)).thenReturn(Optional.empty());

    final var result = target.findByIdAndAccount(itemId, accountId);

    assertThat(result).isNull();
  }

  @Test
  void shouldFindChangesAfterTheRequestedSequence() {
    final var accountId = UUID.randomUUID();
    final var entity = getSecureItemJpaEntity(accountId);
    final var domainItem = getSecureItem(entity);

    when(jpaRepository.findByAccountIdAndChangeSequenceGreaterThanOrderByChangeSequenceAsc(
            eq(accountId), eq(10L), any(Pageable.class)))
        .thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domainItem);

    final var result = target.findChanges(accountId, 10L, 5);

    assertThat(result).containsExactly(domainItem);
    verify(jpaRepository)
        .findByAccountIdAndChangeSequenceGreaterThanOrderByChangeSequenceAsc(
            eq(accountId), eq(10L), any(Pageable.class));
    verify(mapper).toDomain(entity);
  }

  @Test
  void shouldReturnTheNextChangeSequenceForTheAccount() {
    final var accountId = UUID.randomUUID();
    when(jpaRepository.nextChangeSequence(accountId)).thenReturn(42L);

    final var nextSequence = target.nextChangeSequence(accountId);

    assertThat(nextSequence).isEqualTo(42L);
    verify(jpaRepository).nextChangeSequence(accountId);
  }

  @Test
  void shouldFindItemsByAccountUsingFilter() {
    final var accountId = UUID.randomUUID();
    final var filter = getFilter(ItemTypeDto.PASSWORD);

    final var entity = getSecureItemJpaEntity(UUID.randomUUID());

    final var domainItem = getSecureItem(entity);

    final var entitiesPage = new PageImpl<>(List.of(entity));
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(entitiesPage);

    when(mapper.toDomain(entity)).thenReturn(domainItem);

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getItemId()).isEqualTo(entity.getItemId());

    verify(jpaRepository).findAll(any(Specification.class), any(Pageable.class));
    verify(mapper).toDomain(entity);
  }

  @Test
  void shouldSoftDeleteSecureItem() {
    final var itemId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();
    final var deletedAt = Instant.now();

    when(jpaRepository.softDeleteIfRevisionMatches(itemId, accountId, 3L, 4L, 10L, deletedAt))
        .thenReturn(0);

    final var deleted =
        target.softDeleteIfRevisionMatches(itemId, accountId, 3L, 4L, 10L, deletedAt);

    assertThat(deleted).isFalse();
    verify(jpaRepository).softDeleteIfRevisionMatches(itemId, accountId, 3L, 4L, 10L, deletedAt);
  }

  @Test
  void shouldUpdateSecureItem() {
    final var now = Instant.now();

    final var domainItem =
        SecureItem.of(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ItemType.NOTE,
            1,
            "Note",
            "payload".getBytes(),
            now);

    when(jpaRepository.updateIfRevisionMatches(
            domainItem.getItemId(),
            domainItem.getAccountId(),
            1L,
            domainItem.getItemRevision(),
            domainItem.getChangeSequence(),
            domainItem.getItemType().name(),
            domainItem.getSchemaVersion(),
            domainItem.getDisplayHint(),
            domainItem.getPayload(),
            domainItem.getPayloadVersion(),
            domainItem.getUpdatedAt()))
        .thenReturn(0);

    final var updated = target.updateIfRevisionMatches(domainItem, 1L);

    assertThat(updated).isFalse();
    verify(jpaRepository)
        .updateIfRevisionMatches(
            domainItem.getItemId(),
            domainItem.getAccountId(),
            1L,
            domainItem.getItemRevision(),
            domainItem.getChangeSequence(),
            domainItem.getItemType().name(),
            domainItem.getSchemaVersion(),
            domainItem.getDisplayHint(),
            domainItem.getPayload(),
            domainItem.getPayloadVersion(),
            domainItem.getUpdatedAt());
  }

  @Test
  void shouldFindItemsByAccount_withoutAnyOptionalFilters() {
    final var accountId = UUID.randomUUID();
    final var filter =
        new ListSecureItemsFilter(
            null, null, null, Set.of(), false, 50, ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);

    final var entity = getSecureItemJpaEntity(accountId);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(getSecureItem(entity));

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
    verify(jpaRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void shouldFindItemsByAccount_withCreatedAfterFilter() {
    final var accountId = UUID.randomUUID();
    final var createdAfter = Instant.now().minusSeconds(60);

    final var filter =
        new ListSecureItemsFilter(
            createdAfter,
            null,
            null,
            Set.of(),
            false,
            50,
            ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);

    final var entity = getSecureItemJpaEntity(accountId);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(getSecureItem(entity));

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
  }

  @Test
  void shouldFindItemsByAccount_withUpdatedAfterFilter() {
    final var accountId = UUID.randomUUID();
    final var updatedAfter = Instant.now().minusSeconds(60);

    final var filter =
        new ListSecureItemsFilter(
            null,
            updatedAfter,
            null,
            Set.of(),
            false,
            50,
            ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);

    final var entity = getSecureItemJpaEntity(accountId);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(getSecureItem(entity));

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
  }

  @Test
  void shouldFindItemsByAccount_includingDeleted() {
    final var accountId = UUID.randomUUID();

    final var filter =
        new ListSecureItemsFilter(
            null, null, null, Set.of(), true, 50, ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);

    final var entity = getSecureItemJpaEntity(accountId);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(getSecureItem(entity));

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
  }

  @Test
  void shouldFindItemsByAccount_withAllFilters() {
    final var accountId = UUID.randomUUID();

    final var filter =
        new ListSecureItemsFilter(
            Instant.now().minusSeconds(120),
            Instant.now().minusSeconds(60),
            ItemTypeDto.PASSWORD,
            Set.of(),
            false,
            10,
            ListSecureItemsFilter.Order.UPDATED_AT_DESC);

    final var entity = getSecureItemJpaEntity(accountId);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(getSecureItem(entity));

    final var result = target.findFilteredByAccount(accountId, filter);

    assertThat(result).hasSize(1);
  }

  private ListSecureItemsFilter getFilter(final ItemTypeDto type) {
    return new ListSecureItemsFilter(
        null, null, type, Set.of(), false, 100, ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);
  }

  private SecureItemJpaEntity getSecureItemJpaEntity(UUID accountId) {
    return new SecureItemJpaEntity(
        UUID.randomUUID(),
        accountId,
        ItemType.PASSWORD.name(),
        1,
        "GitHub",
        "payload".getBytes(),
        1L,
        Instant.now(),
        Instant.now(),
        null);
  }

  private SecureItem getSecureItem(SecureItemJpaEntity entity) {
    return SecureItem.restore(
        entity.getItemId(),
        entity.getAccountId(),
        ItemType.PASSWORD,
        entity.getSchemaVersion(),
        entity.getDisplayHint(),
        entity.getPayload(),
        entity.getPayloadVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        null);
  }
}
