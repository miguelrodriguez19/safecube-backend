package com.miguelrodriguez19.safecube.user.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.user.application.dto.UpdateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * UpdateUserProfileUseCase
 *
 * <p>Updates mutable attributes of an existing {@link UserProfile}.
 *
 * <p>Only non-deleted profiles can be updated.
 *
 * @see "docs/use-cases/user/user_use_cases_safe_cube_backend_v_1.md"
 */
@Component
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {

  private final UserProfileRepository repository;
  private final UserProfileResponseMapper mapper;

  /**
   * Executes the use case.
   *
   * @param command the command containing update data
   * @return a successful {@link Result} containing the updated profile, or a failure containing
   *     {@link UserError}
   */
  public Result<UserProfileResponse, UserError> execute(final UpdateUserProfileCommand command) {

    final var profileOpt = repository.findByAccountId(command.accountId());
    if (profileOpt.isEmpty()) {
      return Result.failure(new UserError.UserProfileNotFound());
    }

    final UserProfile profile = profileOpt.get();

    profile.updateDisplayName(command.displayName(), command.now());
    repository.save(profile);

    final var response = mapper.mapResponse(profile);
    return Result.success(response);
  }
}
