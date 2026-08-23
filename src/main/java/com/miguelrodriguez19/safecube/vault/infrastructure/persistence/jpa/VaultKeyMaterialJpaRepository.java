package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** VaultKeyMaterialJpaRepository */
public interface VaultKeyMaterialJpaRepository
    extends JpaRepository<VaultKeyMaterialJpaEntity, UUID> {

  @Modifying
  @Transactional
  @Query(
      value =
          """
          UPDATE vault_key_material
             SET kek_enc_master = :newKekEncMaster,
                 master_key_revision = master_key_revision + 1,
                 updated_at = :updatedAt
           WHERE account_id = :accountId
             AND master_key_revision = :expectedMasterKeyRevision
          """,
      nativeQuery = true)
  int updateMasterWrappedKekIfRevisionMatches(
      @Param("accountId") UUID accountId,
      @Param("newKekEncMaster") byte[] newKekEncMaster,
      @Param("expectedMasterKeyRevision") long expectedMasterKeyRevision,
      @Param("updatedAt") Instant updatedAt);
}
