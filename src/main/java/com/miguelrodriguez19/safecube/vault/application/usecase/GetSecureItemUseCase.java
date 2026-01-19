package com.miguelrodriguez19.safecube.vault.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.query.GetSecureItemQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.result.GetSecureItemResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultError;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Use case responsible for retrieving a SecureItem by id for a given account.
 *
 * <p>The vault backend does not interpret the payload. It only enforces ownership isolation and
 * returns the opaque payload with minimal metadata.
 */
@Component
@RequiredArgsConstructor
public class GetSecureItemUseCase {

  private final SecureItemRepository secureItemRepository;
  private final ItemTypeMapper itemTypeMapper;

  public Result<GetSecureItemResult, VaultError> execute(final GetSecureItemQuery query) {
    final var secureItem =
        secureItemRepository.findByIdAndAccount(query.itemId(), query.accountId());

    if (secureItem == null) {
      return Result.failure(new VaultError.SecureItemNotFound());
    }

    return Result.success(
        new GetSecureItemResult(
            secureItem.getItemId(),
            itemTypeMapper.toDto(secureItem.getItemType()),
            secureItem.getSchemaVersion(),
            secureItem.getDisplayHint(),
            secureItem.getPayload(),
            secureItem.getPayloadVersion(),
            secureItem.getUpdatedAt(),
            secureItem.getDeletedAt()));
  }
}
