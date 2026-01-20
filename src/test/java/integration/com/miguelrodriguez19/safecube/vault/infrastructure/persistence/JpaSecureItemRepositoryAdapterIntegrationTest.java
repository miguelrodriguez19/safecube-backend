package integration.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaSecureItemRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest(profiles = {"jpa"})
class JpaSecureItemRepositoryAdapterIntegrationTest {

  @Autowired private JpaSecureItemRepositoryAdapter target;

  @Test
  void shouldPersistAndRetrieveSecureItemByIdAndAccount() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var secureItem =
        SecureItem.of(itemId, accountId, ItemType.PASSWORD, 1, "GitHub", "payload".getBytes(), now);

    target.save(secureItem);

    final var loaded = target.findByIdAndAccount(itemId, accountId);

    assertThat(loaded).isNotNull();
    assertThat(loaded)
        .extracting(
            SecureItem::getItemId,
            SecureItem::getAccountId,
            SecureItem::getItemType,
            SecureItem::getSchemaVersion,
            SecureItem::getDisplayHint,
            SecureItem::getPayloadVersion)
        .containsExactly(itemId, accountId, ItemType.PASSWORD, 1, "GitHub", 1L);
  }

  @Test
  void shouldReturnNull_whenSecureItemDoesNotExist() {
    final var result = target.findByIdAndAccount(UUID.randomUUID(), UUID.randomUUID());
    assertThat(result).isNull();
  }

  @Test
  void shouldListAllItemsByAccount_includingDeleted() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();
    final var filters =
        new ListSecureItemsFilter(null, null, Set.of(), false, 100, Order.DISPLAY_NAME_ASC);

    final var first =
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload-1".getBytes(),
            now);

    final var second =
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "Note",
            "payload-2".getBytes(),
            now.plusSeconds(5));

    target.save(first);
    target.save(second);

    final var listed = target.findFilteredByAccount(accountId, filters);

    assertThat(listed).hasSize(2);
    assertThat(listed)
        .extracting(SecureItem::getItemId)
        .containsExactlyInAnyOrder(first.getItemId(), second.getItemId());
  }

  @Test
  void shouldSoftDeleteSecureItem() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var secureItem =
        SecureItem.of(itemId, accountId, ItemType.NOTE, 1, "ToDelete", "payload".getBytes(), now);

    target.save(secureItem);

    final var deletedAt = now.plusSeconds(30).truncatedTo(ChronoUnit.MICROS);
    target.softDelete(itemId, accountId, deletedAt);

    final var loaded = target.findByIdAndAccount(itemId, accountId);

    assertThat(loaded).isNotNull();
    assertThat(loaded.getDeletedAt()).isEqualTo(deletedAt);
  }
}
