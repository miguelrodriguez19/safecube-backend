package unit.com.miguelrodriguez19.safecube.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.user.application.dto.CreateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.AccountExistencePort;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.application.usecase.CreateUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class CreateUserProfileUseCaseTest {

  @Mock private UserProfileRepository repository;
  @Mock private AccountExistencePort accountExistencePort;
  @Mock private UserProfileResponseMapper mapper;

  @InjectMocks private CreateUserProfileUseCase target;

  @Test
  void shouldCreateUserProfile_whenNotExists() {
    final var command = getCreateUserProfileCommand();

    when(accountExistencePort.existsByAccountId(command.accountId())).thenReturn(true);
    when(repository.findByAccountId(command.accountId())).thenReturn(Optional.empty());

    final var profile =
        UserProfile.of(command.userId(), command.accountId(), command.displayName(), command.now());
    final var mockUserProfileResponse = mock(UserProfileResponse.class);
    when(mapper.mapResponse(profile)).thenReturn(mockUserProfileResponse);

    final var result = target.execute(command);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success()).contains(mockUserProfileResponse);

    verify(repository).save(profile);
  }

  @Test
  void shouldFail_whenAccountDoesNotExist() {
    final var command = getCreateUserProfileCommand();

    when(accountExistencePort.existsByAccountId(command.accountId())).thenReturn(false);

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error().get()).isInstanceOf(UserError.AccountNotFound.class);

    verifyNoInteractions(repository);
  }

  @Test
  void shouldFail_whenProfileAlreadyExists() {
    final var command = getCreateUserProfileCommand();

    when(accountExistencePort.existsByAccountId(command.accountId())).thenReturn(true);

    final var mockUserProfile = mock(UserProfile.class);
    when(repository.findByAccountId(command.accountId())).thenReturn(Optional.of(mockUserProfile));

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error().get()).isInstanceOf(UserError.UserProfileAlreadyExists.class);

    verify(repository, never()).save(any(UserProfile.class));
  }

  private CreateUserProfileCommand getCreateUserProfileCommand() {
    return new CreateUserProfileCommand(
        UUID.randomUUID(), UUID.randomUUID(), "John Doe", Instant.now());
  }
}
