package unit.com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.DeleteSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.SecureItemMutationHasher;
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
  @Mock SecureItemMutationRepository mutationRepository;
  @Mock SecureItemMutationHasher mutationHasher;

  @InjectMocks private DeleteSecureItemUseCase target;

  @Test
  void shouldSoftDeleteUsingAtomicItemRevision() {
    final var command = command();
    when(mutationHasher.hash(command)).thenReturn("hash");
    when(secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId()))
        .thenReturn(existing(command));
    when(secureItemRepository.nextChangeSequence(command.accountId())).thenReturn(50L);
    when(secureItemRepository.softDeleteIfRevisionMatches(
            command.itemId(), command.accountId(), 3L, 4L, 50L, command.deletedAt()))
        .thenReturn(true);

    final var result = target.execute(command);

    assertThat(result.success().orElseThrow().payloadVersion()).isEqualTo(9L);
    assertThat(result.success().orElseThrow().itemRevision()).isEqualTo(4L);
    verify(mutationRepository).save(any());
  }

  @Test
  void shouldRejectStaleDeleteWithoutRecordingMutation() {
    final var command = command();
    when(mutationHasher.hash(command)).thenReturn("hash");
    when(secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId()))
        .thenReturn(existing(command));
    when(secureItemRepository.nextChangeSequence(command.accountId())).thenReturn(50L);

    final var result = target.execute(command);

    assertThat(result.error()).containsInstanceOf(VaultError.StaleDeleteRejected.class);
    verify(mutationRepository, never()).save(any());
  }

  private DeleteSecureItemCommand command() {
    return new DeleteSecureItemCommand(
        UUID.randomUUID(),
        UUID.randomUUID(),
        3L,
        UUID.randomUUID(),
        Instant.parse("2026-01-18T10:00:00Z"));
  }

  private SecureItem existing(final DeleteSecureItemCommand command) {
    return SecureItem.restore(
        command.itemId(),
        command.accountId(),
        ItemType.NOTE,
        1,
        "Note",
        "encrypted".getBytes(),
        9L,
        3L,
        40L,
        command.deletedAt().minusSeconds(120),
        command.deletedAt().minusSeconds(60),
        null);
  }
}
