package unit.com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository.StoredMutation;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.CreateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.SecureItemMutationHasher;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class CreateSecureItemUseCaseTest {

  @Mock SecureItemRepository secureItemRepository;
  @Mock SecureItemMutationRepository mutationRepository;
  @Mock ItemTypeMapper itemTypeMapper;
  @Mock SecureItemMutationHasher mutationHasher;

  @InjectMocks private CreateSecureItemUseCase target;

  @Test
  void shouldStoreClientPayloadVersionAndInitialServerRevision() {
    final var now = Instant.parse("2026-01-18T10:00:00Z");
    final var command =
        new CreateSecureItemCommand(
            UUID.randomUUID(),
            ItemTypeDto.PASSWORD,
            1,
            "GitHub",
            "encrypted".getBytes(),
            7L,
            UUID.randomUUID(),
            now);
    when(mutationHasher.hash(command)).thenReturn("hash");
    when(itemTypeMapper.toDomain(ItemTypeDto.PASSWORD)).thenReturn(ItemType.PASSWORD);
    when(secureItemRepository.nextChangeSequence(command.accountId())).thenReturn(41L);

    final var result = target.execute(command);

    final var item =
        ArgumentCaptor.forClass(
            com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem.class);
    verify(secureItemRepository).save(item.capture());
    verify(mutationRepository).save(any());
    assertThat(item.getValue().getPayloadVersion()).isEqualTo(7L);
    assertThat(item.getValue().getItemRevision()).isEqualTo(1L);
    assertThat(item.getValue().getChangeSequence()).isEqualTo(41L);
    assertThat(result.success().orElseThrow())
        .extracting(
            CreateSecureItemResult::mutationId,
            CreateSecureItemResult::payloadVersion,
            CreateSecureItemResult::itemRevision,
            CreateSecureItemResult::changeSequence)
        .containsExactly(command.mutationId(), 7L, 1L, 41L);
  }

  @Test
  void shouldReturnOriginalResponseForIdenticalRetryWithoutExecutingCreateAgain() {
    final var command =
        new CreateSecureItemCommand(
            UUID.randomUUID(),
            ItemTypeDto.NOTE,
            1,
            "Note",
            "encrypted".getBytes(),
            4L,
            UUID.randomUUID(),
            Instant.parse("2026-01-18T10:00:00Z"));
    final var itemId = UUID.randomUUID();
    final var originalTime = command.createdAt().minusSeconds(30);
    when(mutationHasher.hash(command)).thenReturn("same-hash");
    when(mutationRepository.findByAccountAndMutationId(command.accountId(), command.mutationId()))
        .thenReturn(
            new StoredMutation(
                command.accountId(),
                command.mutationId(),
                itemId,
                "CREATE",
                "same-hash",
                4L,
                1L,
                31L,
                originalTime,
                null));

    final var result = target.execute(command).success().orElseThrow();

    assertThat(result.itemId()).isEqualTo(itemId);
    assertThat(result.updatedAt()).isEqualTo(originalTime);
    assertThat(result.changeSequence()).isEqualTo(31L);
    verify(secureItemRepository, never()).nextChangeSequence(any());
    verify(secureItemRepository, never()).save(any());
    verify(mutationRepository, never()).save(any());
  }

  @Test
  void shouldRejectMutationIdReusedWithDifferentCanonicalRequest() {
    final var command =
        new CreateSecureItemCommand(
            UUID.randomUUID(),
            ItemTypeDto.NOTE,
            1,
            "Note",
            "encrypted".getBytes(),
            4L,
            UUID.randomUUID(),
            Instant.parse("2026-01-18T10:00:00Z"));
    when(mutationHasher.hash(command)).thenReturn("new-hash");
    when(mutationRepository.findByAccountAndMutationId(command.accountId(), command.mutationId()))
        .thenReturn(
            new StoredMutation(
                command.accountId(),
                command.mutationId(),
                UUID.randomUUID(),
                "CREATE",
                "old-hash",
                4L,
                1L,
                31L,
                command.createdAt(),
                null));

    final var result = target.execute(command);

    assertThat(result.error()).containsInstanceOf(VaultError.IdempotencyConflict.class);
    verify(secureItemRepository, never()).save(any());
  }
}
