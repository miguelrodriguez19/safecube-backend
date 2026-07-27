package unit.com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.SecureItemMutationHasher;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.UpdateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class UpdateSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock SecureItemMutationRepository mutationRepository;
  @Mock ItemTypeMapper itemTypeMapper;
  @Mock SecureItemMutationHasher mutationHasher;

  @InjectMocks private UpdateSecureItemUseCase target;

  @Test
  void shouldUseAtomicRevisionAndKeepClientPayloadVersion() {
    final var command = command(5L, 12L);
    when(mutationHasher.hash(command)).thenReturn("hash");
    when(secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId()))
        .thenReturn(existing(command, 5L));
    when(itemTypeMapper.toDomain(command.itemTypeDto())).thenReturn(ItemType.PASSWORD);
    when(secureItemRepository.nextChangeSequence(command.accountId())).thenReturn(80L);
    when(secureItemRepository.updateIfRevisionMatches(any(), eq(command.expectedItemRevision())))
        .thenReturn(true);

    final var result = target.execute(command);

    assertThat(result.success().orElseThrow().payloadVersion()).isEqualTo(12L);
    assertThat(result.success().orElseThrow().itemRevision()).isEqualTo(6L);
    assertThat(result.success().orElseThrow().changeSequence()).isEqualTo(80L);
    verify(mutationRepository).save(any());
  }

  @Test
  void shouldRejectStaleCompareAndSetWithoutRecordingMutation() {
    final var command = command(5L, 12L);
    when(mutationHasher.hash(command)).thenReturn("hash");
    when(secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId()))
        .thenReturn(existing(command, 6L));
    when(itemTypeMapper.toDomain(command.itemTypeDto())).thenReturn(ItemType.PASSWORD);
    when(secureItemRepository.nextChangeSequence(command.accountId())).thenReturn(81L);
    when(secureItemRepository.updateIfRevisionMatches(any(), eq(command.expectedItemRevision())))
        .thenReturn(false);

    final var result = target.execute(command);

    assertThat(result.error()).containsInstanceOf(VaultError.StaleUpdateRejected.class);
    verify(mutationRepository, never()).save(any());
  }

  private UpdateSecureItemCommand command(final long baseRevision, final long payloadVersion) {
    return new UpdateSecureItemCommand(
        UUID.randomUUID(),
        UUID.randomUUID(),
        ItemTypeDto.PASSWORD,
        1,
        "GitHub",
        "encrypted".getBytes(),
        payloadVersion,
        baseRevision,
        UUID.randomUUID(),
        Instant.parse("2026-01-18T10:00:00Z"));
  }

  private SecureItem existing(final UpdateSecureItemCommand command, final long itemRevision) {
    return SecureItem.restore(
        command.itemId(),
        command.accountId(),
        ItemType.PASSWORD,
        1,
        "GitHub",
        "old".getBytes(),
        11L,
        itemRevision,
        70L,
        command.updatedAt().minusSeconds(60),
        command.updatedAt().minusSeconds(30),
        null);
  }
}
