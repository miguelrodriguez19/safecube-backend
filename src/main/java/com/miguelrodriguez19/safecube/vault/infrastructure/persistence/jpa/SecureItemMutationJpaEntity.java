package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vault_item_mutations")
public class SecureItemMutationJpaEntity {

  @EmbeddedId private SecureItemMutationId id;

  @Column(nullable = false)
  private java.util.UUID itemId;

  @Column(nullable = false, length = 16)
  private String operation;

  @Column(nullable = false, length = 64)
  private String requestHash;

  @Column(nullable = false)
  private long payloadVersion;

  @Column(nullable = false)
  private long itemRevision;

  @Column(nullable = false)
  private long changeSequence;

  @Column(nullable = false)
  private Instant occurredAt;

  @Column private Instant deletedAt;
}
