package com.miguelrodriguez19.safecube.user.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.user.application.dto.DeleteUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * DeleteUserProfileUseCase
 *
 * <p>Performs a logical deletion of an existing {@link
 * com.miguelrodriguez19.safecube.user.domain.model.UserProfile}.
 *
 * <p>After deletion, the profile is considered inactive and cannot be modified.
 *
 * @see "docs/use-cases/user/user_use_cases_safe_cube_backend_v_1.md"
 */
@Component
@RequiredArgsConstructor
public class DeleteUserProfileUseCase {

  private final UserProfileRepository repository;

  /**
   * Executes the use case.
   *
   * @param command the command containing deletion data
   * @return a successful {@link Result} with no payload, or a failure containing {@link
   *     UserError.UserProfileNotFound}
   */
  public Result<Void, UserError> execute(final DeleteUserProfileCommand command) {

    final var profileOpt = repository.findByAccountId(command.accountId());
    if (profileOpt.isEmpty()) {
      return Result.failure(new UserError.UserProfileNotFound());
    }

    final var profile = profileOpt.get();

    profile.delete(command.now());
    repository.save(profile);

    return Result.success(Void.INSTANCE);
  }
}
