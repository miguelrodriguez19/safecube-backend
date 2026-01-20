package com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.InitVaultKeyMaterialCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * InitVaultKeyMaterialUseCase
 *
 * <p>Initializes vault key material for an account.
 */
@Component
@RequiredArgsConstructor
public class InitVaultKeyMaterialUseCase {

  private final VaultKeyMaterialRepository repository;

  public Result<Void, VaultKeyMaterialError> execute(final InitVaultKeyMaterialCommand command) {

    final var existing = repository.findByAccountId(command.accountId());
    if (existing.isPresent()) {
      return Result.failure(new VaultKeyMaterialError.VaultAlreadyInitialized());
    }

    final var keyMaterial =
        VaultKeyMaterial.create(
            command.accountId(),
            command.kekEncMaster(),
            command.kekEncRecovery(),
            command.kdfAlgorithm(),
            command.kdfSalt(),
            command.kdfMemoryKib(),
            command.kdfIterations(),
            command.kdfParallelism(),
            command.kdfOutputLen(),
            command.cryptoVersion(),
            command.createdAt());

    repository.save(keyMaterial);
    return Result.success(Void.INSTANCE);
  }
}
