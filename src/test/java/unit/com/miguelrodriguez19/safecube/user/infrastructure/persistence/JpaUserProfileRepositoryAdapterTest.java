package unit.com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.JpaUserProfileRepositoryAdapter;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaEntity;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaRepository;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.mapper.UserProfileMapper;
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

  @InjectMocks private JpaUserProfileRepositoryAdapter target;

  @Test
  void shouldReturnTrue_whenProfileExists() {
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findByAccountId(accountId))
        .thenReturn(Optional.of(new UserProfileJpaEntity()));

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_whenProfileDoesNotExist() {
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnMappedDomain_whenEntityExists() {
    final var accountId = UUID.randomUUID();

    final var entity = new UserProfileJpaEntity();
    final var domain = mock(UserProfile.class);

    when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    final var result = target.findByAccountId(accountId);

    assertThat(result).contains(domain);
  }

  @Test
  void shouldReturnEmpty_whenEntityDoesNotExist() {
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

    final var result = target.findByAccountId(accountId);

    assertThat(result).isEmpty();
    verifyNoInteractions(mapper);
  }

  @Test
  void shouldMapDomainAndPersistEntity() {
    final var profile = mock(UserProfile.class);
    final var entity = new UserProfileJpaEntity();

    when(mapper.toEntity(profile)).thenReturn(entity);

    target.save(profile);

    verify(jpaRepository).save(entity);
  }
}
