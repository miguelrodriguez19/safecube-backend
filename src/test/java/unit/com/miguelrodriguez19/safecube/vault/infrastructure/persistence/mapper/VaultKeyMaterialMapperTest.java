package unit.com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaEntity;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.VaultKeyMaterialMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import unit.annotation.UnitTest;

@UnitTest
class VaultKeyMaterialMapperTest {

  private final VaultKeyMaterialMapper target = new VaultKeyMaterialMapper();

  @Test
  void shouldMapDomainToEntity() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();

    final var domain =
        VaultKeyMaterial.create(
            accountId,
            new byte[] {1},
            new byte[] {2},
            "ARGON2ID",
            new byte[] {3},
            65536,
            3,
            1,
            32,
            "v1",
            now);

    final var entity = target.toEntity(domain);

    assertThat(entity)
        .extracting(
            VaultKeyMaterialJpaEntity::getAccountId,
            VaultKeyMaterialJpaEntity::getKekEncMaster,
            VaultKeyMaterialJpaEntity::getKekEncRecovery,
            VaultKeyMaterialJpaEntity::getKdfAlgorithm,
            VaultKeyMaterialJpaEntity::getKdfMemoryKib,
            VaultKeyMaterialJpaEntity::getKdfIterations,
            VaultKeyMaterialJpaEntity::getKdfParallelism,
            VaultKeyMaterialJpaEntity::getKdfOutputLen,
            VaultKeyMaterialJpaEntity::getCryptoVersion,
            VaultKeyMaterialJpaEntity::getCreatedAt,
            VaultKeyMaterialJpaEntity::getUpdatedAt,
            VaultKeyMaterialJpaEntity::getMasterKeyRevision)
        .containsExactly(
            accountId,
            domain.getKekEncMaster(),
            domain.getKekEncRecovery(),
            "ARGON2ID",
            65536,
            3,
            1,
            32,
            "v1",
            now,
            now,
            1L);
  }

  @Test
  void shouldMapEntityToDomain() {
    final var now = Instant.now();
    final var accountId = UUID.randomUUID();

    final var entity =
        new VaultKeyMaterialJpaEntity(
            accountId,
            new byte[] {1},
            new byte[] {2},
            "ARGON2ID",
            new byte[] {3},
            65536,
            3,
            1,
            32,
            "v1",
            now,
            now,
            7L);

    final var domain = target.toDomain(entity);

    assertThat(domain)
        .extracting(
            VaultKeyMaterial::getAccountId,
            VaultKeyMaterial::getKekEncMaster,
            VaultKeyMaterial::getKekEncRecovery,
            VaultKeyMaterial::getKdfAlgorithm,
            VaultKeyMaterial::getKdfMemoryKib,
            VaultKeyMaterial::getKdfIterations,
            VaultKeyMaterial::getKdfParallelism,
            VaultKeyMaterial::getKdfOutputLen,
            VaultKeyMaterial::getCryptoVersion,
            VaultKeyMaterial::getCreatedAt,
            VaultKeyMaterial::getUpdatedAt,
            VaultKeyMaterial::getMasterKeyRevision)
        .containsExactly(
            accountId,
            entity.getKekEncMaster(),
            entity.getKekEncRecovery(),
            "ARGON2ID",
            65536,
            3,
            1,
            32,
            "v1",
            now,
            now,
            7L);
  }
}
