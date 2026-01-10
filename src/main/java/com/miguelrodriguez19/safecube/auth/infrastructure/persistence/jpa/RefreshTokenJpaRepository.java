package com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

  Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

  @Modifying
  @Query(
      """
      update RefreshTokenJpaEntity t
         set t.revokedAt = :revokedAt
       where t.accountId = :accountId
         and t.revokedAt is null
      """)
  int revokeAllByAccountId(
      @Param("accountId") final UUID accountId, @Param("revokedAt") final Instant revokedAt);
}
