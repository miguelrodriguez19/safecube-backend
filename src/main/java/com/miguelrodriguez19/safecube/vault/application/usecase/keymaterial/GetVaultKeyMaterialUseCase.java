package com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialQuery;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.GetVaultKeyMaterialResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GetVaultKeyMaterialUseCase
 *
 * <p>Retrieves vault key material for an account.
 */
@Component
@RequiredArgsConstructor
public class GetVaultKeyMaterialUseCase {

  private final VaultKeyMaterialRepository repository;

  public Result<GetVaultKeyMaterialResult, VaultKeyMaterialError> execute(
      final GetVaultKeyMaterialQuery query) {

    final var keyMaterialOpt = repository.findByAccountId(query.accountId());
    if (keyMaterialOpt.isEmpty()) {
      return Result.failure(new VaultKeyMaterialError.VaultNotInitialized());
    }

    final var keyMaterial = keyMaterialOpt.get();

    final var result =
        new GetVaultKeyMaterialResult(
            keyMaterial.getAccountId(),
            keyMaterial.getKekEncMaster(),
            keyMaterial.getKekEncRecovery(),
            keyMaterial.getKdfAlgorithm(),
            keyMaterial.getKdfSalt(),
            keyMaterial.getKdfMemoryKib(),
            keyMaterial.getKdfIterations(),
            keyMaterial.getKdfParallelism(),
            keyMaterial.getKdfOutputLen(),
            keyMaterial.getCryptoVersion(),
            keyMaterial.getCreatedAt(),
            keyMaterial.getUpdatedAt(),
            keyMaterial.getMasterKeyRevision());

    return Result.success(result);
  }
}
