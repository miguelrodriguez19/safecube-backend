package unit.com.miguelrodriguez19.safecube.vault.domain.model.secureitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidPayloadException;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class SecureItemTest {

  @Test
  void shouldCreateSecureItem_givenValidPayload() {
    final var itemId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();
    final var payload = "encrypted-payload".getBytes();
    final var now = Instant.now();

    final var secureItem =
        SecureItem.of(itemId, accountId, ItemType.PASSWORD, 1, "My password", payload, now);

    assertThat(secureItem)
        .extracting(
            SecureItem::getItemId,
            SecureItem::getAccountId,
            SecureItem::getPayloadVersion,
            SecureItem::getCreatedAt,
            SecureItem::getUpdatedAt,
            SecureItem::getDeletedAt)
        .containsExactly(itemId, accountId, 1L, now, now, null);
  }

  @Test
  void shouldRejectCreation_givenNullPayload() {
    final var now = Instant.now();

    assertThatThrownBy(
            () ->
                SecureItem.of(
                    UUID.randomUUID(), UUID.randomUUID(), ItemType.GENERIC, 1, null, null, now))
        .isInstanceOf(InvalidPayloadException.class);
  }

  @Test
  void shouldRejectCreation_givenEmptyPayload() {
    final var now = Instant.now();

    assertThatThrownBy(
            () ->
                SecureItem.of(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    ItemType.GENERIC,
                    1,
                    null,
                    new byte[0],
                    now))
        .isInstanceOf(InvalidPayloadException.class);
  }

  @Test
  void shouldRejectCreation_givenPayloadExceedingMaxSize() {
    final var now = Instant.now();
    final var oversizedPayload = new byte[1_048_577]; // +1MB

    assertThatThrownBy(
            () ->
                SecureItem.of(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    ItemType.GENERIC,
                    1,
                    null,
                    oversizedPayload,
                    now))
        .isInstanceOf(InvalidPayloadException.class);
  }

  @Test
  void shouldReturnFalseForIsDeleted_givenActiveItem() {
    final var secureItem =
        SecureItem.of(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ItemType.NOTE,
            1,
            null,
            "payload".getBytes(),
            Instant.now());

    assertThat(secureItem.isDeleted()).isFalse();
  }

  @Test
  void shouldReturnTrueForIsDeleted_givenRestoredDeletedItem() {
    final var secureItem =
        SecureItem.restore(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ItemType.NOTE,
            1,
            null,
            "payload".getBytes(),
            3L,
            Instant.now().minusSeconds(100),
            Instant.now().minusSeconds(50),
            Instant.now());

    assertThat(secureItem.isDeleted()).isTrue();
  }
}
