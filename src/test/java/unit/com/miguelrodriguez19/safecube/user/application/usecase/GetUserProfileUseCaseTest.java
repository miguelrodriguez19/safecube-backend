package unit.com.miguelrodriguez19.safecube.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.mapper.UserProfileResponseMapper;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.application.usecase.GetUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class GetUserProfileUseCaseTest {

  @Mock private UserProfileRepository repository;
  @Mock private UserProfileResponseMapper mapper;

  @InjectMocks private GetUserProfileUseCase target;

  @Test
  void shouldReturnUserProfile_whenProfileExists() {
    final var accountId = UUID.randomUUID();
    final var now = Instant.now();

    final var profile = UserProfile.of(UUID.randomUUID(), accountId, "John Doe", now);
    when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

    final var response = mock(UserProfileResponse.class);
    when(mapper.mapResponse(profile)).thenReturn(response);

    final var result = target.execute(accountId);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success()).contains(response);

    verifyNoMoreInteractions(repository, mapper);
  }

  @Test
  void shouldFail_whenUserProfileDoesNotExist() {
    final var accountId = UUID.randomUUID();

    when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

    final var result = target.execute(accountId);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(UserError.UserProfileNotFound.class);

    verifyNoInteractions(mapper);
  }
}
