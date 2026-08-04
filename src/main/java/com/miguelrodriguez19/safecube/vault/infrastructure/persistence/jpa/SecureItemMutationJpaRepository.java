package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecureItemMutationJpaRepository
    extends JpaRepository<SecureItemMutationJpaEntity, SecureItemMutationId> {

  Optional<SecureItemMutationJpaEntity> findByIdAccountIdAndIdMutationId(
      UUID accountId, UUID mutationId);

  @Query(
      value =
          """
          select 1
            from pg_advisory_xact_lock(
              hashtextextended(cast(:accountId as text) || ':' || cast(:mutationId as text), 0)
            )
          """,
      nativeQuery = true)
  int acquireTransactionLock(
      @Param("accountId") UUID accountId, @Param("mutationId") UUID mutationId);
}
