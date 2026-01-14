package unit.com.miguelrodriguez19.safecube.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.user.application.dto.UpdateUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.application.usecase.UpdateUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class UpdateUserProfileUseCaseTest {

  @Mock private UserProfileRepository repository;
  @Mock private UserProfileResponseMapper mapper;

  @InjectMocks private UpdateUserProfileUseCase target;

  @Test
  void shouldUpdateUserProfileSuccessfully() {
    final var userId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();
    final var initialDisplayName = "John";
    final var createdAt = Instant.now().minusSeconds(60);
    final var now = Instant.now();

    final var profile = UserProfile.of(userId, accountId, initialDisplayName, createdAt);
    when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

    final var mockUserProfileResponse = mock(UserProfileResponse.class);
    when(mapper.mapResponse(profile)).thenReturn(mockUserProfileResponse);

    final var command = getUpdateUserProfileCommand(accountId, now);
    final var result = target.execute(command);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.success()).contains(mockUserProfileResponse);
  }

  @Test
  void shouldFail_whenUserProfileDoesNotExist() {
    final var accountId = UUID.randomUUID();
    final var now = Instant.now();

    when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

    final var command = getUpdateUserProfileCommand(accountId, now);

    final var result = target.execute(command);

    assertThat(result.isFailure()).isTrue();
    assertThat(result.error()).containsInstanceOf(UserError.UserProfileNotFound.class);

    verifyNoInteractions(mapper);
  }

  private UpdateUserProfileCommand getUpdateUserProfileCommand(
      final UUID accountId, final Instant now) {
    return new UpdateUserProfileCommand(accountId, "John Doe", now);
  }
}
