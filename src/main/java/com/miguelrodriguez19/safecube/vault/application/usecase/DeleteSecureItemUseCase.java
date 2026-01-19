package com.miguelrodriguez19.safecube.vault.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.command.DeleteSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.result.DeleteSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for soft-deleting a SecureItem.
 *
 * <p>The deletion is logical (soft delete) and enforced through optimistic concurrency based on
 * {@code updatedAt}. Once deleted, the item cannot be updated or restored.
 */
@Component
@RequiredArgsConstructor
public class DeleteSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;

  public Result<DeleteSecureItemResult, VaultError> execute(final DeleteSecureItemCommand command) {

    final var existingItem =
        secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId());

    if (existingItem == null) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    if (!command.deletedAt().isAfter(existingItem.getUpdatedAt())) {
      return Result.failure(new VaultError.StaleDeleteRejected());
    }

    secureItemRepository.softDelete(command.itemId(), command.accountId(), command.deletedAt());

    return Result.success(new DeleteSecureItemResult(command.itemId(), command.deletedAt()));
  }
}
