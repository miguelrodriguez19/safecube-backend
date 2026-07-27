package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SecureItemJpaEntity
 *
 * <p>JPA entity representing a persisted vault SecureItem. The payload is stored as an opaque
 * encrypted BYTEA and is never interpreted by the backend.
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vault_items")
public class SecureItemJpaEntity {

  @Id private UUID itemId;

  @Column(nullable = false)
  private UUID accountId;

  @Column(nullable = false, length = 50)
  private String itemType;

  @Column(nullable = false)
  private int schemaVersion;

  @Column(nullable = false, length = 255)
  private String displayHint;

  @Column(nullable = false)
  private byte[] payload;

  @Column(nullable = false)
  private long payloadVersion;

  @Column(nullable = false)
  private long itemRevision;

  @Column(nullable = false)
  private long changeSequence;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column private Instant deletedAt;

  public SecureItemJpaEntity(
      final UUID itemId,
      final UUID accountId,
      final String itemType,
      final int schemaVersion,
      final String displayHint,
      final byte[] payload,
      final long payloadVersion,
      final Instant createdAt,
      final Instant updatedAt,
      final Instant deletedAt) {
    this(
        itemId,
        accountId,
        itemType,
        schemaVersion,
        displayHint,
        payload,
        payloadVersion,
        payloadVersion,
        payloadVersion,
        createdAt,
        updatedAt,
        deletedAt);
  }
}
