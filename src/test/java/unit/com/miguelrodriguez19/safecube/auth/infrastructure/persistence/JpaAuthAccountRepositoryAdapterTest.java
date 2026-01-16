package unit.com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.JpaAuthAccountRepositoryAdapter;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.mapper.AuthAccountMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

/**
 * JpaAuthAccountRepositoryAdapterTest
 *
 * <p>Unit tests for the JPA adapter implementing the application port.
 */
@UnitTest
class JpaAuthAccountRepositoryAdapterTest {

  @Mock private AuthAccountJpaRepository jpaRepository;

  @Mock private AuthAccountMapper mapper;

  @InjectMocks private JpaAuthAccountRepositoryAdapter target;

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldCheckExistsByEmail(final boolean expected) {
    final var email = "exists@safecube.io";
    when(jpaRepository.existsByEmail(email)).thenReturn(expected);

    final var result = target.existsByEmail(email);

    assertThat(result).isEqualTo(expected);
    verify(jpaRepository).existsByEmail(email);
    verifyNoMoreInteractions(jpaRepository);
  }

  @Test
  void shouldReturnEmpty_whenFindByEmailReturnsEmpty() {
    final var email = "missing@safecube.io";
    when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

    final var result = target.findByEmail(email);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldMapToDomain_whenFindByEmailReturnsEntity() {
    final var now = Instant.parse("2026-01-09T00:00:00Z");
    final var entity =
        new AuthAccountJpaEntity(UUID.randomUUID(), "test@safecube.io", "hash", true, now, null);

    when(jpaRepository.findByEmail(entity.getEmail())).thenReturn(Optional.of(entity));

    final var mockAuthAccount = mock(AuthAccount.class);
    when(mapper.toDomain(entity)).thenReturn(mockAuthAccount);

    final var result = target.findByEmail(entity.getEmail());

    assertThat(result).isPresent();

    final var domain = result.orElseThrow();
    assertThat(domain).isEqualTo(mockAuthAccount);
  }

  @Test
  void shouldMapAndSaveEntity_whenSavingDomainAccount() {
    final var now = Instant.parse("2026-01-09T00:00:00Z");
    final var account = AuthAccount.of("save@safecube.io", "hash", now);

    final var mockEntity = mock(AuthAccountJpaEntity.class);
    when(mapper.toEntity(account)).thenReturn(mockEntity);

    when(jpaRepository.save(mockEntity)).thenReturn(mockEntity);

    assertDoesNotThrow(() -> target.save(account));
  }

  @Test
  void shouldFindAccountById() {
    final var accountId = UUID.randomUUID();

    final var mockAccount = mock(AuthAccountJpaEntity.class);
    when(jpaRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotFindAccountById() {
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findById(accountId)).thenReturn(Optional.empty());

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isFalse();
  }
}
