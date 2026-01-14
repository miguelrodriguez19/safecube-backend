package unit.com.miguelrodriguez19.safecube.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.shared.result.Result;
import com.miguelrodriguez19.safecube.shared.result.Void;
import com.miguelrodriguez19.safecube.user.application.dto.DeleteUserProfileCommand;
import com.miguelrodriguez19.safecube.user.application.error.UserError;
import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.application.usecase.DeleteUserProfileUseCase;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class DeleteUserProfileUseCaseTest {

  @Mock private UserProfileRepository repository;

  @InjectMocks private DeleteUserProfileUseCase target;

  @Test
  void shouldDeleteUserProfileSuccessfully() {
    final var userId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();
    final var displayName = "John";
    final var createdAt = Instant.now().minusSeconds(120);
    final var now = Instant.now();

    final var profile = UserProfile.of(userId, accountId, displayName, createdAt);
    when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

    final var command = new DeleteUserProfileCommand(accountId, now);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success()).contains(Void.INSTANCE);

    assertThat(profile)
        .extracting(UserProfile::getDeletedAt, UserProfile::getUpdatedAt)
        .containsExactly(now, now);
  }

  @Test
  void shouldFail_whenUserProfileDoesNotExist() {
    final var accountId = UUID.randomUUID();
    final var now = Instant.now();

    when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

    final var command = new DeleteUserProfileCommand(accountId, now);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(UserError.UserProfileNotFound.class);
  }
}
