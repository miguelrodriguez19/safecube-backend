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
 * VaultKeyMaterialJpaEntity
 *
 * <p>JPA entity representing persisted vault key material. All cryptographic data is stored as
 * opaque blobs.
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vault_key_material")
public class VaultKeyMaterialJpaEntity {

  @Id private UUID accountId;

  @Column(nullable = false)
  private byte[] kekEncMaster;

  @Column(nullable = false)
  private byte[] kekEncRecovery;

  @Column(nullable = false, length = 50)
  private String kdfAlgorithm;

  @Column(nullable = false)
  private byte[] kdfSalt;

  @Column(nullable = false)
  private int kdfMemoryKib;

  @Column(nullable = false)
  private int kdfIterations;

  @Column(nullable = false)
  private int kdfParallelism;

  @Column(nullable = false)
  private int kdfOutputLen;

  @Column(nullable = false, length = 50)
  private String cryptoVersion;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(name = "master_key_revision", nullable = false)
  private long masterKeyRevision;
}
