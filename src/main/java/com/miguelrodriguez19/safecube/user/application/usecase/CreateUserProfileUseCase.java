package com.miguelrodriguez19.safecube.user.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.user.application.dto.CreateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.AccountExistencePort;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.domain.exception.InvalidDisplayNameException;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * CreateUserProfileUseCase
 *
 * <p>Creates a user profile for an existing account if it does not already exist.
 *
 * @see "docs/use-cases/user/user_use_cases_safe_cube_backend_v_1.md"
 */
@Component
@RequiredArgsConstructor
public class CreateUserProfileUseCase {

  private final UserProfileRepository repository;
  private final AccountExistencePort accountExistencePort;
  private final UserProfileResponseMapper mapper;

  public Result<UserProfileResponse, UserError> execute(final CreateUserProfileCommand command) {

    if (!accountExistencePort.existsByAccountId(command.accountId())) {
      return Result.failure(new UserError.AccountNotFound());
    }

    if (repository.findByAccountId(command.accountId()).isPresent()) {
      return Result.failure(new UserError.UserProfileAlreadyExists());
    }

    final UserProfile profile;
    try {
      profile =
          UserProfile.of(
              command.userId(), command.accountId(), command.displayName(), command.now());
    } catch (InvalidDisplayNameException e) {
      return Result.failure(new UserError.InvalidDisplayName());
    }

    repository.save(profile);

    final var response = mapper.mapResponse(profile);
    return Result.success(response);
  }
}
