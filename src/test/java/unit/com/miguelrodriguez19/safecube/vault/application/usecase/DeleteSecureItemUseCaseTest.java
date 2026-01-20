package unit.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.result.DeleteSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.DeleteSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class DeleteSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;

  @InjectMocks private DeleteSecureItemUseCase target;

  @Test
  void shouldSoftDeleteSecureItem_givenNewerDeletedAt() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var updatedAt = Instant.parse("2026-01-18T10:00:00Z");
    final var deletedAt = Instant.parse("2026-01-18T10:05:00Z");

    final var existingItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload".getBytes(),
            1L,
            updatedAt.minusSeconds(60),
            updatedAt,
            null);

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(existingItem);

    final var command = getDeleteSecureItemCommand(accountId, itemId, deletedAt);

    final var result = target.execute(command);

    verify(secureItemRepository).softDelete(itemId, accountId, deletedAt);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success().get())
        .extracting(DeleteSecureItemResult::itemId, DeleteSecureItemResult::deletedAt)
        .containsExactly(itemId, deletedAt);
  }

  @Test
  void shouldRejectDelete_whenDeletedAtIsStale() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    final var timestamp = Instant.parse("2026-01-18T10:00:00Z");

    final var existingItem =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "Note",
            "payload".getBytes(),
            1L,
            timestamp,
            timestamp,
            null);

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(existingItem);

    final var command = getDeleteSecureItemCommand(accountId, itemId, timestamp);

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(VaultError.StaleDeleteRejected.class);
  }

  @Test
  void shouldReturnNotFound_whenItemDoesNotExist() {
    final var accountId = UUID.randomUUID();
    final var itemId = UUID.randomUUID();

    when(secureItemRepository.findByIdAndAccount(itemId, accountId)).thenReturn(null);

    final var command = getDeleteSecureItemCommand(accountId, itemId, Instant.now());

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(VaultError.SecureItemNotFound.class);
  }

  private DeleteSecureItemCommand getDeleteSecureItemCommand(
      final UUID accountId, final UUID itemId, final Instant timestamp) {
    return new DeleteSecureItemCommand(accountId, itemId, timestamp);
  }
}
