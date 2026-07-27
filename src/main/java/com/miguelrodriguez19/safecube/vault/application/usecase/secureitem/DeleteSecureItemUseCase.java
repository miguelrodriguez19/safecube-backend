package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.DeleteSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository.StoredMutation;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for soft-deleting a SecureItem.
 *
 * <p>The deletion is logical (soft delete) and enforced through compare-and-set on the server-owned
 * item revision. Once deleted, the item cannot be updated or restored.
 */
@Component
@RequiredArgsConstructor
public class DeleteSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;
  private final SecureItemMutationRepository mutationRepository;
  private final SecureItemMutationHasher mutationHasher;

  @Transactional
  public Result<DeleteSecureItemResult, VaultError> execute(final DeleteSecureItemCommand command) {
    final var requestHash = mutationHasher.hash(command);
    mutationRepository.lock(command.accountId(), command.mutationId());
    final var storedMutation =
        mutationRepository.findByAccountAndMutationId(command.accountId(), command.mutationId());
    if (storedMutation != null) {
      if (!storedMutation.operation().equals("DELETE")
          || !storedMutation.requestHash().equals(requestHash)) {
        return Result.failure(new VaultError.IdempotencyConflict());
      }
      return Result.success(
          new DeleteSecureItemResult(
              storedMutation.itemId(),
              storedMutation.mutationId(),
              storedMutation.payloadVersion(),
              storedMutation.itemRevision(),
              storedMutation.changeSequence(),
              storedMutation.deletedAt()));
    }

    final var existingItem =
        secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId());

    if (existingItem == null) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    if (existingItem.isDeleted()) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    final var nextItemRevision = command.expectedItemRevision() + 1;
    final var changeSequence = secureItemRepository.nextChangeSequence(command.accountId());
    if (!secureItemRepository.softDeleteIfRevisionMatches(
        command.itemId(),
        command.accountId(),
        command.expectedItemRevision(),
        nextItemRevision,
        changeSequence,
        command.deletedAt())) {
      return Result.failure(new VaultError.StaleDeleteRejected());
    }

    mutationRepository.save(
        new StoredMutation(
            command.accountId(),
            command.mutationId(),
            command.itemId(),
            "DELETE",
            requestHash,
            existingItem.getPayloadVersion(),
            nextItemRevision,
            changeSequence,
            command.deletedAt(),
            command.deletedAt()));

    return Result.success(
        new DeleteSecureItemResult(
            command.itemId(),
            command.mutationId(),
            existingItem.getPayloadVersion(),
            nextItemRevision,
            changeSequence,
            command.deletedAt()));
  }
}
