package com.miguelrodriguez19.safecube.vault.application.usecase.keymaterial;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekCommand;
import com.miguelrodriguez19.safecube.vault.application.dto.keymaterial.UpdateMasterWrappedKekResult;
import com.miguelrodriguez19.safecube.vault.application.error.VaultKeyMaterialError;
import com.miguelrodriguez19.safecube.vault.application.port.out.VaultKeyMaterialRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UpdateMasterWrappedKekUseCase
 *
 * <p>Updates the KEK wrapped with the master key (e.g. after passphrase change).
 */
@Component
@RequiredArgsConstructor
public class UpdateMasterWrappedKekUseCase {

  private final VaultKeyMaterialRepository repository;

  @Transactional
  public Result<UpdateMasterWrappedKekResult, VaultKeyMaterialError> execute(
      final UpdateMasterWrappedKekCommand command) {

    if (!repository.existsByAccountId(command.accountId())) {
      return Result.failure(new VaultKeyMaterialError.VaultNotInitialized());
    }

    VaultKeyMaterial.validateMasterWrappedKek(command.newKekEncMaster());

    final var updatedRows =
        repository.updateMasterWrappedKekIfRevisionMatches(
            command.accountId(),
            command.newKekEncMaster(),
            command.expectedMasterKeyRevision(),
            command.updatedAt());

    if (updatedRows != 1) {
      return Result.failure(new VaultKeyMaterialError.StaleMasterWrappedKekUpdate());
    }

    return Result.success(
        new UpdateMasterWrappedKekResult(command.expectedMasterKeyRevision() + 1));
  }
}
