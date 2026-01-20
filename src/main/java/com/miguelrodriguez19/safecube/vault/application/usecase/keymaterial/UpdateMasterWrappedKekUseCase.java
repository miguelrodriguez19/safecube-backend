package com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * UpdateMasterWrappedKekUseCase
 *
 * <p>Updates the KEK wrapped with the master key (e.g. after passphrase change).
 */
@Component
@RequiredArgsConstructor
public class UpdateMasterWrappedKekUseCase {

  private final VaultKeyMaterialRepository repository;

  public Result<Void, VaultKeyMaterialError> execute(final UpdateMasterWrappedKekCommand command) {

    final var keyMaterialOpt = repository.findByAccountId(command.accountId());
    if (keyMaterialOpt.isEmpty()) {
      return Result.failure(new VaultKeyMaterialError.VaultNotInitialized());
    }

    final var keyMaterial = keyMaterialOpt.get();
    keyMaterial.rotateMasterWrappedKek(command.newKekEncMaster(), command.updatedAt());

    repository.update(keyMaterial);
    return Result.success(Void.INSTANCE);
  }
}
