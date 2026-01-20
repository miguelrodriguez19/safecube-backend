package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.UpdateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.UpdateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for updating an existing SecureItem.
 *
 * <p>This use case enforces optimistic concurrency based on {@code updatedAt} and increments the
 * payload version on each successful update.
 */
@Component
@RequiredArgsConstructor
public class UpdateSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;
  private final ItemTypeMapper itemTypeMapper;

  public Result<UpdateSecureItemResult, VaultError> execute(final UpdateSecureItemCommand command) {

    final var existingItem =
        secureItemRepository.findByIdAndAccount(command.itemId(), command.accountId());

    if (existingItem == null) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    if (!command.updatedAt().isAfter(existingItem.getUpdatedAt())) {
      return Result.failure(new VaultError.StaleUpdateRejected());
    }

    final var itemType = itemTypeMapper.toDomain(command.itemTypeDto());

    final var updatedItem =
        SecureItem.restore(
            existingItem.getItemId(),
            existingItem.getAccountId(),
            itemType,
            command.schemaVersion(),
            command.displayHint(),
            command.payload(),
            existingItem.getPayloadVersion() + 1,
            existingItem.getCreatedAt(),
            command.updatedAt(),
            existingItem.getDeletedAt());

    secureItemRepository.update(updatedItem);

    return Result.success(
        new UpdateSecureItemResult(
            updatedItem.getItemId(), updatedItem.getPayloadVersion(), updatedItem.getUpdatedAt()));
  }
}
