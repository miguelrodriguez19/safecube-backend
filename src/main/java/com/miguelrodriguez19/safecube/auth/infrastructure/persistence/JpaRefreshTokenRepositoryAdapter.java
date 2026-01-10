package com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRecord;
import com.miguelrodriguez19.safecube.auth.application.port.out.RefreshTokenRepository;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.RefreshTokenJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JpaRefreshTokenRepositoryAdapter
 *
 * <p>JPA-backed implementation of {@link RefreshTokenRepository}.
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository jpaRepository;

  @Override
  public void save(
      final UUID tokenId,
      final UUID accountId,
      final String tokenHash,
      final Instant expiresAt,
      final Instant createdAt) {

    jpaRepository.save(
        new RefreshTokenJpaEntity(tokenId, accountId, tokenHash, expiresAt, createdAt, null));
  }

  @Override
  public Optional<RefreshTokenRecord> findByTokenHash(final String tokenHash) {

    return jpaRepository
        .findByTokenHash(tokenHash)
        .map(
            entity ->
                new RefreshTokenRecord(
                    entity.getTokenId(),
                    entity.getAccountId(),
                    entity.getTokenHash(),
                    entity.getExpiresAt(),
                    entity.getRevokedAt()));
  }

  @Override
  public void revoke(final UUID tokenId, final Instant revokedAt) {
    jpaRepository
        .findById(tokenId)
        .ifPresent(
            entity ->
                jpaRepository.save(
                    new RefreshTokenJpaEntity(
                        entity.getTokenId(),
                        entity.getAccountId(),
                        entity.getTokenHash(),
                        entity.getExpiresAt(),
                        entity.getCreatedAt(),
                        revokedAt)));
  }

  @Override
  public void revokeAllByAccountId(final UUID accountId, final Instant revokedAt) {

    jpaRepository.revokeAllByAccountId(accountId, revokedAt);
  }
}
