package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsQuery;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.ListSecureItemsUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.SecureItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class ListSecureItemsUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;

  @InjectMocks private ListSecureItemsUseCase target;

  @Test
  void shouldExcludeDeletedItemsByDefault_andApplySinceFilter() {
    final var accountId = UUID.randomUUID();

    final var createdAt = Instant.parse("2026-01-18T10:00:00Z");
    final var updatedAt = Instant.parse("2026-01-18T10:05:00Z");
    final var deletedAt = Instant.parse("2026-01-18T10:10:00Z");

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

    final var deletedItem =
        SecureItem.restore(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "Note",
            "payload".getBytes(),
            1L,
            createdAt,
            deletedAt,
            deletedAt);

    when(secureItemRepository.findByAccount(accountId))
        .thenReturn(List.of(activeItem, deletedItem));

    final var query = getListSecureItemsQuery(accountId, createdAt);
    final var result = target.execute(query);

    verify(secureItemRepository).findByAccount(accountId);

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

    when(secureItemRepository.findByAccount(accountId)).thenReturn(List.of(deletedItem));

    final var query = new ListSecureItemsQuery(accountId, null, true);
    final var result = target.execute(query);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get().items()).hasSize(1);
    assertThat(result.success().get().items().getFirst().deletedAt()).isEqualTo(now);
  }

  private ListSecureItemsQuery getListSecureItemsQuery(
      final UUID accountId, final Instant createdAt) {
    return new ListSecureItemsQuery(accountId, createdAt, false);
  }
}
