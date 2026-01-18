package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * SecureItemJpaRepository
 *
 * <p>Spring Data repository for {@link SecureItemJpaEntity}.
 */
public interface SecureItemJpaRepository extends JpaRepository<SecureItemJpaEntity, UUID> {

  Optional<SecureItemJpaEntity> findByItemIdAndAccountId(UUID itemId, UUID accountId);

  List<SecureItemJpaEntity> findAllByAccountId(UUID accountId);

  @Modifying
  @Transactional
  @Query(
      """
      update SecureItemJpaEntity e
         set e.deletedAt = :deletedAt
       where e.itemId = :itemId
         and e.accountId = :accountId
      """)
  int softDelete(
      @Param("itemId") UUID itemId,
      @Param("accountId") UUID accountId,
      @Param("deletedAt") Instant deletedAt);
}
