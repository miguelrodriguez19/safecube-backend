package unit.com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.SecureItemMutationHasher;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SecureItemMutationHasherTest {

  private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final SecureItemMutationHasher target = new SecureItemMutationHasher();

  @Test
  void shouldIncludeEveryCreateRequestFieldInTheCanonicalHash() {
    final var base = create(ACCOUNT_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L);
    final var hash = target.hash(base);

    assertThat(hash).hasSize(64);
    assertThat(
            target.hash(create(otherAccountId(), ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L)))
        .isNotEqualTo(hash);
    assertThat(target.hash(create(ACCOUNT_ID, ItemTypeDto.NOTE, 3, "hint", "payload", 7L)))
        .isNotEqualTo(hash);
    assertThat(target.hash(create(ACCOUNT_ID, ItemTypeDto.PASSWORD, 4, "hint", "payload", 7L)))
        .isNotEqualTo(hash);
    assertThat(target.hash(create(ACCOUNT_ID, ItemTypeDto.PASSWORD, 3, "other", "payload", 7L)))
        .isNotEqualTo(hash);
    assertThat(target.hash(create(ACCOUNT_ID, ItemTypeDto.PASSWORD, 3, "hint", "changed", 7L)))
        .isNotEqualTo(hash);
    assertThat(target.hash(create(ACCOUNT_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 8L)))
        .isNotEqualTo(hash);
  }

  @Test
  void shouldIncludeEveryUpdateRequestFieldInTheCanonicalHash() {
    final var base =
        update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L, 5L);
    final var hash = target.hash(base);

    assertThat(hash).hasSize(64);
    assertThat(
            target.hash(
                update(
                    otherAccountId(), ITEM_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(
                    ACCOUNT_ID, otherItemId(), ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.NOTE, 3, "hint", "payload", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 4, "hint", "payload", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 3, "other", "payload", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 3, "hint", "changed", 7L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 8L, 5L)))
        .isNotEqualTo(hash);
    assertThat(
            target.hash(
                update(ACCOUNT_ID, ITEM_ID, ItemTypeDto.PASSWORD, 3, "hint", "payload", 7L, 6L)))
        .isNotEqualTo(hash);
  }

  @Test
  void shouldIncludeEveryDeleteRequestFieldInTheCanonicalHash() {
    final var base = delete(ACCOUNT_ID, ITEM_ID, 5L);
    final var hash = target.hash(base);

    assertThat(hash).hasSize(64);
    assertThat(target.hash(delete(otherAccountId(), ITEM_ID, 5L))).isNotEqualTo(hash);
    assertThat(target.hash(delete(ACCOUNT_ID, otherItemId(), 5L))).isNotEqualTo(hash);
    assertThat(target.hash(delete(ACCOUNT_ID, ITEM_ID, 6L))).isNotEqualTo(hash);
  }

  private CreateSecureItemCommand create(
      final UUID accountId,
      final ItemTypeDto itemType,
      final int schemaVersion,
      final String displayHint,
      final String payload,
      final long payloadVersion) {
    return new CreateSecureItemCommand(
        accountId,
        itemType,
        schemaVersion,
        displayHint,
        payload.getBytes(),
        payloadVersion,
        UUID.randomUUID(),
        Instant.EPOCH);
  }

  private UpdateSecureItemCommand update(
      final UUID accountId,
      final UUID itemId,
      final ItemTypeDto itemType,
      final int schemaVersion,
      final String displayHint,
      final String payload,
      final long payloadVersion,
      final long itemRevision) {
    return new UpdateSecureItemCommand(
        accountId,
        itemId,
        itemType,
        schemaVersion,
        displayHint,
        payload.getBytes(),
        payloadVersion,
        itemRevision,
        UUID.randomUUID(),
        Instant.EPOCH);
  }

  private DeleteSecureItemCommand delete(
      final UUID accountId, final UUID itemId, final long itemRevision) {
    return new DeleteSecureItemCommand(
        accountId, itemId, itemRevision, UUID.randomUUID(), Instant.EPOCH);
  }

  private UUID otherAccountId() {
    return UUID.fromString("00000000-0000-0000-0000-000000000003");
  }

  private UUID otherItemId() {
    return UUID.fromString("00000000-0000-0000-0000-000000000004");
  }
}
