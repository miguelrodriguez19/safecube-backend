package unit.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
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
  void shouldFindItemsByAccountUsingFilter() {
    final var accountId = UUID.randomUUID();
    final var filter = getFilter(ItemTypeDto.PASSWORD);

    final var entity =
        new SecureItemJpaEntity(
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

    final var domainItem =
        SecureItem.restore(
            entity.getItemId(),
            entity.getAccountId(),
            ItemType.PASSWORD,
            1,
            "GitHub",
            entity.getPayload(),
            1L,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            null);

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

    target.softDelete(itemId, accountId, deletedAt);

    verify(jpaRepository).softDelete(itemId, accountId, deletedAt);
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

    target.update(domainItem);

    verify(mapper).toEntity(domainItem);
    verify(jpaRepository).save(entity);
  }

  private ListSecureItemsFilter getFilter(final ItemTypeDto type) {
    return new ListSecureItemsFilter(
        null, type, Set.of(), false, 100, ListSecureItemsFilter.Order.DISPLAY_NAME_ASC);
  }
}
