package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository.StoredMutation;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidPayloadException;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for creating a new SecureItem.
 *
 * <p>This use case validates the payload, initializes versioning and timestamps, persists the item
 * and returns its identifier.
 */
@Component
@RequiredArgsConstructor
public class CreateSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;
  private final SecureItemMutationRepository mutationRepository;
  private final ItemTypeMapper itemTypeMapper;
  private final SecureItemMutationHasher mutationHasher;

  @Transactional
  public Result<CreateSecureItemResult, VaultError> execute(final CreateSecureItemCommand command) {
    try {
      final var requestHash = mutationHasher.hash(command);
      mutationRepository.lock(command.accountId(), command.mutationId());
      final var storedMutation =
          mutationRepository.findByAccountAndMutationId(command.accountId(), command.mutationId());
      if (storedMutation != null) {
        if (!storedMutation.operation().equals("CREATE")
            || !storedMutation.requestHash().equals(requestHash)) {
          return Result.failure(new VaultError.IdempotencyConflict());
        }
        return Result.success(
            new CreateSecureItemResult(
                storedMutation.itemId(),
                storedMutation.mutationId(),
                storedMutation.payloadVersion(),
                storedMutation.itemRevision(),
                storedMutation.changeSequence(),
                storedMutation.occurredAt()));
      }

      final var itemId = UUID.randomUUID();
      final var itemType = itemTypeMapper.toDomain(command.itemTypeDto());
      final var changeSequence = secureItemRepository.nextChangeSequence(command.accountId());

      final var secureItem =
          SecureItem.of(
              itemId,
              command.accountId(),
              itemType,
              command.schemaVersion(),
              command.displayHint(),
              command.payload(),
              command.payloadVersion(),
              changeSequence,
              command.createdAt());

      secureItemRepository.save(secureItem);
      mutationRepository.save(
          new StoredMutation(
              command.accountId(),
              command.mutationId(),
              itemId,
              "CREATE",
              requestHash,
              secureItem.getPayloadVersion(),
              secureItem.getItemRevision(),
              secureItem.getChangeSequence(),
              secureItem.getCreatedAt(),
              null));

      return Result.success(
          new CreateSecureItemResult(
              itemId,
              command.mutationId(),
              secureItem.getPayloadVersion(),
              secureItem.getItemRevision(),
              secureItem.getChangeSequence(),
              secureItem.getCreatedAt()));
    } catch (final InvalidPayloadException exception) {
      return Result.failure(new VaultError.InvalidPayload());
    }
  }
}
