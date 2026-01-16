package unit.com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.JpaUserProfileRepositoryAdapter;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaEntity;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaRepository;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.mapper.UserProfileMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class JpaUserProfileRepositoryAdapterTest {

  @Mock private UserProfileJpaRepository jpaRepository;
  @Mock private UserProfileMapper mapper;

  @InjectMocks private JpaUserProfileRepositoryAdapter adapter;

  @Test
  void shouldFindUserProfileByAccountId() {
    final var userId = UUID.randomUUID();
    final var accountId = UUID.randomUUID();
    final var displayName = "John";
    final var createdAt = Instant.now();

    final var entity =
        new UserProfileJpaEntity(userId, accountId, displayName, createdAt, createdAt);

    when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.of(entity));

    final var mockUserProfile = mock(UserProfile.class);
    when(mapper.toDomain(entity)).thenReturn(mockUserProfile);

    final var result = adapter.findByAccountId(accountId);

    assertThat(result).contains(mockUserProfile);
  }
}
