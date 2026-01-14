package com.miguelrodriguez19.safecube.user.application.usecase;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GetUserProfileUseCase
 *
 * <p>Retrieves an existing {@link UserProfile} by account identifier.
 *
 * @see "docs/use-cases/user/user_use_cases_safe_cube_backend_v_1.md"
 */
@Component
@RequiredArgsConstructor
public class GetUserProfileUseCase {

  private final UserProfileRepository repository;
  private final UserProfileResponseMapper mapper;

  /**
   * Executes the use case.
   *
   * @param accountId the account identifier
   * @return a successful {@link Result} containing the user profile, or a failure containing {@link
   *     UserError.UserProfileNotFound}
   */
  public Result<UserProfileResponse, UserError> execute(final UUID accountId) {
    return repository
        .findByAccountId(accountId)
        .map(mapper::mapResponse)
        .map(Result::<UserProfileResponse, UserError>success)
        .orElseGet(() -> Result.failure(new UserError.UserProfileNotFound()));
  }
}
