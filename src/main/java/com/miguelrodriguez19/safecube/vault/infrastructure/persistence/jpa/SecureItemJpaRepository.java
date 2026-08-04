package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * SecureItemJpaRepository
 *
 * <p>Spring Data repository for {@link SecureItemJpaEntity}.
 */
public interface SecureItemJpaRepository
    extends JpaRepository<SecureItemJpaEntity, UUID>,
        JpaSpecificationExecutor<SecureItemJpaEntity> {

  Optional<SecureItemJpaEntity> findByItemIdAndAccountId(UUID itemId, UUID accountId);

  List<SecureItemJpaEntity> findByAccountIdAndChangeSequenceGreaterThanOrderByChangeSequenceAsc(
      UUID accountId, long changeSequence, Pageable pageable);

  @Query(
      value =
          """
          with next_cursor as (
            insert into vault_item_change_cursors (account_id, last_sequence)
            values (:accountId, 1)
            on conflict (account_id)
            do update
               set last_sequence = vault_item_change_cursors.last_sequence + 1
            returning last_sequence
          )
          select last_sequence from next_cursor
          """,
      nativeQuery = true)
  long nextChangeSequence(@Param("accountId") UUID accountId);

  @Modifying
  @Transactional
  @Query(
      """
      update SecureItemJpaEntity e
         set e.itemType = :itemType,
             e.schemaVersion = :schemaVersion,
             e.displayHint = :displayHint,
             e.payload = :payload,
             e.payloadVersion = :payloadVersion,
             e.itemRevision = :nextItemRevision,
             e.changeSequence = :changeSequence,
             e.updatedAt = :updatedAt
       where e.itemId = :itemId
         and e.accountId = :accountId
         and e.itemRevision = :expectedItemRevision
         and e.deletedAt is null
      """)
  int updateIfRevisionMatches(
      @Param("itemId") UUID itemId,
      @Param("accountId") UUID accountId,
      @Param("expectedItemRevision") long expectedItemRevision,
      @Param("nextItemRevision") long nextItemRevision,
      @Param("changeSequence") long changeSequence,
      @Param("itemType") String itemType,
      @Param("schemaVersion") int schemaVersion,
      @Param("displayHint") String displayHint,
      @Param("payload") byte[] payload,
      @Param("payloadVersion") long payloadVersion,
      @Param("updatedAt") Instant updatedAt);

  @Modifying
  @Transactional
  @Query(
      """
      update SecureItemJpaEntity e
         set e.deletedAt = :deletedAt,
             e.updatedAt = :deletedAt,
             e.itemRevision = :nextItemRevision,
             e.changeSequence = :changeSequence
       where e.itemId = :itemId
         and e.accountId = :accountId
         and e.itemRevision = :expectedItemRevision
         and e.deletedAt is null
      """)
  int softDeleteIfRevisionMatches(
      @Param("itemId") UUID itemId,
      @Param("accountId") UUID accountId,
      @Param("expectedItemRevision") long expectedItemRevision,
      @Param("nextItemRevision") long nextItemRevision,
      @Param("changeSequence") long changeSequence,
      @Param("deletedAt") Instant deletedAt);
}
