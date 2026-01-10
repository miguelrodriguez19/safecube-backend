package unit.com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.JpaRefreshTokenRepositoryAdapter;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.RefreshTokenJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class JpaRefreshTokenRepositoryAdapterTest {

  @Mock private RefreshTokenJpaRepository jpaRepository;

  @InjectMocks private JpaRefreshTokenRepositoryAdapter target;

  @Test
  void shouldSaveRefreshToken() {
    target.save(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "hash",
        Instant.now().plusSeconds(3600),
        Instant.now());

    verify(jpaRepository).save(any(RefreshTokenJpaEntity.class));
  }

  @Test
  void shouldFindByTokenHash() {
    final var entity =
        new RefreshTokenJpaEntity(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hash",
            Instant.now().plusSeconds(3600),
            Instant.now(),
            null);

    when(jpaRepository.findByTokenHash("hash")).thenReturn(Optional.of(entity));

    final var result = target.findByTokenHash("hash");

    assertThat(result).isPresent();
    assertThat(result.get().tokenId()).isEqualTo(entity.getTokenId());
  }
}
