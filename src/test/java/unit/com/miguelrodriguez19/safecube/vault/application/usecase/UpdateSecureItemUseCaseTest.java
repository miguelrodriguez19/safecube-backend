package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.result.UpdateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.UpdateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class UpdateSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock ItemTypeMapper itemTypeMapper;

  @InjectMocks private UpdateSecureItemUseCase target;

  @Test
  void shouldUpdateSecureItem_givenNewerUpdatedAt() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var createdAt = Instant.parse("2026-01-18T09:00:00Z");
    final var previousUpdatedAt = Instant.parse("2026-01-18T10:00:00Z");
    final var newUpdatedAt = Instant.parse("2026-01-18T10:05:00Z");

    final var existingItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "old-payload".getBytes(),
            1L,
            createdAt,
            previousUpdatedAt,
            null);

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(existingItem);

    final var command =
        new UpdateSecureItemCommand(
            accountId,
            itemId,
            ItemTypeDto.PASSWORD,
            2,
            "GitHub Updated",
            "new-payload".getBytes(),
            newUpdatedAt);

    final var result = target.execute(command);

    final var captor = ArgumentCaptor.forClass(SecureItem.class);
    verify(secureItemRepository).update(captor.capture());

    final var updatedItem = captor.getValue();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get())
        .extracting(
            UpdateSecureItemResult::itemId,
            UpdateSecureItemResult::payloadVersion,
            UpdateSecureItemResult::updatedAt)
        .containsExactly(itemId, 2L, newUpdatedAt);

    assertThat(updatedItem.getPayloadVersion()).isEqualTo(2L);
    assertThat(updatedItem.getUpdatedAt()).isEqualTo(newUpdatedAt);
  }

  @Test
  void shouldRejectUpdate_whenUpdatedAtIsStale() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var updatedAt = Instant.parse("2026-01-18T10:00:00Z");

    final var existingItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload".getBytes(),
            1L,
            updatedAt,
            updatedAt,
            null);

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(existingItem);

    final var command = getUpdateSecureItemCommand(accountId, itemId, updatedAt);

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(VaultError.StaleUpdateRejected.class);
  }

  @Test
  void shouldReturnNotFound_whenItemDoesNotExist() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(null);

    final var command = getUpdateSecureItemCommand(accountId, itemId, Instant.now());

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(VaultError.SecureItemNotFound.class);
  }

  private UpdateSecureItemCommand getUpdateSecureItemCommand(
      final UUID accountId, final UUID itemId, final Instant updatedAt) {
    return new UpdateSecureItemCommand(
        accountId,
        itemId,
        ItemTypeDto.PASSWORD,
        1,
        "GitHub",
        "payload".getBytes(),
        updatedAt.minus(30, ChronoUnit.MINUTES));
  }
}
