package com.miguelrodriguez19.safecube.vault.application.usecase.secureitem;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.result.CreateSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import com.miguelrodriguez19.safecube.vault.domain.exception.InvalidPayloadException;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
  private final ItemTypeMapper itemTypeMapper;

  public Result<CreateSecureItemResult, VaultError> execute(final CreateSecureItemCommand command) {
    try {
      final var itemId = UUID.randomUUID();
      final var itemType = itemTypeMapper.toDomain(command.itemTypeDto());

      final var secureItem =
          SecureItem.of(
              itemId,
              command.accountId(),
              itemType,
              command.schemaVersion(),
              command.displayHint(),
              command.payload(),
              command.createdAt());

      secureItemRepository.save(secureItem);

      return Result.success(new CreateSecureItemResult(itemId, secureItem.getCreatedAt()));
    } catch (final InvalidPayloadException exception) {
      return Result.failure(new VaultError.InvalidPayload());
    }
  }
}
