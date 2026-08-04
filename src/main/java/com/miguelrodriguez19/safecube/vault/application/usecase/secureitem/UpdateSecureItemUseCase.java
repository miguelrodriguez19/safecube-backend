package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.UpdateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository.StoredMutation;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for updating an existing SecureItem.
 *
 * <p>This use case enforces optimistic concurrency with the server-owned item revision while
 * preserving the client-owned payload version unchanged.
 */
@Component
@RequiredArgsConstructor
public class UpdateSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;
  private final SecureItemMutationRepository mutationRepository;
  private final ItemTypeMapper itemTypeMapper;
  private final SecureItemMutationHasher mutationHasher;

  @Transactional
  public Result<UpdateSecureItemResult, VaultError> execute(final UpdateSecureItemCommand command) {
    final var requestHash = mutationHasher.hash(command);
    mutationRepository.lock(command.accountId(), command.mutationId());
    final var storedMutation =
        mutationRepository.findByAccountAndMutationId(command.accountId(), command.mutationId());
    if (storedMutation != null) {
      if (!storedMutation.operation().equals("UPDATE")
          || !storedMutation.requestHash().equals(requestHash)) {
        return Result.failure(new VaultError.IdempotencyConflict());
      }
      return Result.success(
          new UpdateSecureItemResult(
              storedMutation.itemId(),
              storedMutation.mutationId(),
              storedMutation.payloadVersion(),
              storedMutation.itemRevision(),
              storedMutation.changeSequence(),
              storedMutation.occurredAt()));
    }

    final var existingItem =
        secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId());

    if (existingItem == null || existingItem.isDeleted()) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    final var itemType = itemTypeMapper.toDomain(command.itemTypeDto());
    final var changeSequence = secureItemRepository.nextChangeSequence(command.accountId());

    final var updatedItem =
        SecureItem.restore(
            existingItem.getItemId(),
            existingItem.getAccountId(),
            itemType,
            command.schemaVersion(),
            command.displayHint(),
            command.payload(),
            command.payloadVersion(),
            command.expectedItemRevision() + 1,
            changeSequence,
            existingItem.getCreatedAt(),
            command.updatedAt(),
            existingItem.getDeletedAt());

    if (!secureItemRepository.updateIfRevisionMatches(
        updatedItem, command.expectedItemRevision())) {
      return Result.failure(new VaultError.StaleUpdateRejected());
    }

    mutationRepository.save(
        new StoredMutation(
            command.accountId(),
            command.mutationId(),
            command.itemId(),
            "UPDATE",
            requestHash,
            updatedItem.getPayloadVersion(),
            updatedItem.getItemRevision(),
            updatedItem.getChangeSequence(),
            updatedItem.getUpdatedAt(),
            null));

    return Result.success(
        new UpdateSecureItemResult(
            updatedItem.getItemId(),
            command.mutationId(),
            updatedItem.getPayloadVersion(),
            updatedItem.getItemRevision(),
            updatedItem.getChangeSequence(),
            updatedItem.getUpdatedAt()));
  }
}
