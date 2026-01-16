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
import org.mockito.ArgumentCaptor;
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
    final var entity = getRefreshTokenJpaEntity(UUID.randomUUID(), "hash");

    when(jpaRepository.findByTokenHash("hash")).thenReturn(Optional.of(entity));

    final var result = target.findByTokenHash("hash");

    assertThat(result).isPresent();
    assertThat(result.get().tokenId()).isEqualTo(entity.getTokenId());
  }

  @Test
  void shouldRevokeRefreshToken() {
    final var tokenId = UUID.randomUUID();
    final var revokedAt = Instant.now();

    final var existingEntity = getRefreshTokenJpaEntity(tokenId, "token-hash");
    when(jpaRepository.findById(tokenId)).thenReturn(Optional.of(existingEntity));

    target.revoke(tokenId, revokedAt);

    final var captor = ArgumentCaptor.forClass(RefreshTokenJpaEntity.class);
    verify(jpaRepository).save(captor.capture());

    final var savedEntity = captor.getValue();

    assertThat(savedEntity)
        .extracting(
            RefreshTokenJpaEntity::getTokenId,
            RefreshTokenJpaEntity::getAccountId,
            RefreshTokenJpaEntity::getTokenHash,
            RefreshTokenJpaEntity::getExpiresAt,
            RefreshTokenJpaEntity::getCreatedAt,
            RefreshTokenJpaEntity::getRevokedAt)
        .containsExactly(
            existingEntity.getTokenId(),
            existingEntity.getAccountId(),
            existingEntity.getTokenHash(),
            existingEntity.getExpiresAt(),
            existingEntity.getCreatedAt(),
            revokedAt);
  }

  @Test
  void shouldRevokeAllRefreshTokensByAccountId() {
    final var accountId = UUID.randomUUID();
    final var revokedAt = Instant.now();

    target.revokeAllByAccountId(accountId, revokedAt);

    verify(jpaRepository).revokeAllByAccountId(accountId, revokedAt);
  }

  private RefreshTokenJpaEntity getRefreshTokenJpaEntity(
      final UUID tokenId, final String tokenHash) {
    return new RefreshTokenJpaEntity(
        tokenId,
        UUID.randomUUID(),
        tokenHash,
        Instant.now().plusSeconds(3600),
        Instant.now(),
        null);
  }
}
