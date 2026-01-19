package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.ListSecureItemsUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class ListSecureItemsUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock ItemTypeMapper itemTypeMapper;

  @InjectMocks private ListSecureItemsUseCase target;

  @Test
  void shouldExcludeDeleted() {
    final var accountId = UUID.randomUUID();

    final var createdAt = Instant.parse("2026-01-18T10:00:00Z");
    final var updatedAt = Instant.parse("2026-01-18T10:05:00Z");

    final var activeItem =
        SecureItem.restore(
            UUID.randomUUID(),
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload".getBytes(),
            1L,
            createdAt,
            updatedAt,
            null);

    final var query = getListSecureItemsQuery(accountId, createdAt, false);

    when(secureItemRepository.findFilteredByAccount(accountId, query.filter()))
        .thenReturn(List.of(activeItem));

    final var result = target.execute(query);

    verify(secureItemRepository).findFilteredByAccount(accountId, query.filter());

    assertThat(result.isSuccess()).isTrue();

    final var items = result.success().get().items();

    assertThat(items).hasSize(1);
    assertThat(items.get(0).itemId()).isEqualTo(activeItem.getItemId());
  }

  @Test
  void shouldIncludeDeletedItems_whenRequested() {
    final var accountId = UUID.randomUUID();

    final var now = Instant.parse("2026-01-18T10:00:00Z");

    final var deletedItem =
        SecureItem.restore(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "Deleted",
            "payload".getBytes(),
            1L,
            now,
            now,
            now);

    final var query = getListSecureItemsQuery(accountId, now, true);
    when(secureItemRepository.findFilteredByAccount(accountId, query.filter()))
        .thenReturn(List.of(deletedItem));

    final var result = target.execute(query);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get().items()).hasSize(1);
    assertThat(result.success().get().items().getFirst().deletedAt()).isEqualTo(now);
  }

  private static @NonNull ListSecureItemsFilter getFilter() {
    return new ListSecureItemsFilter(null, null, Set.of(), true, 100, Order.DISPLAY_NAME_ASC);
  }

  private ListSecureItemsQuery getListSecureItemsQuery(
      final UUID accountId, final Instant createdAt, boolean includeDeleted) {
    final var filters =
        new ListSecureItemsFilter(
            createdAt, null, Set.of(), includeDeleted, 100, Order.DISPLAY_NAME_ASC);

    return new ListSecureItemsQuery(accountId, filters);
  }
}
