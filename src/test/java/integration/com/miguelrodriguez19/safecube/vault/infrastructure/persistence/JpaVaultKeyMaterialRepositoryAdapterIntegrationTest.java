package integration.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaVaultKeyMaterialRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest(profiles = {"jpa"})
class JpaVaultKeyMaterialRepositoryAdapterIntegrationTest {

  @Autowired private AuthAccountJpaRepository authAccountJpaRepository;

  @Autowired private JpaVaultKeyMaterialRepositoryAdapter target;

  @Test
  void shouldPersistAndLoadVaultKeyMaterial() {
    final var now = Instant.now();
    final var accountId = insertAuthAccount();

    final var material =
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

    target.save(material);

    final var loaded = target.findByAccountId(accountId);

    assertThat(loaded).isPresent();
    assertThat(loaded.get())
        .extracting(
            VaultKeyMaterial::getAccountId,
            VaultKeyMaterial::getKdfAlgorithm,
            VaultKeyMaterial::getCryptoVersion)
        .containsExactly(accountId, "ARGON2ID", "v1");
  }

  @Test
  void shouldReturnEmpty_whenVaultKeyMaterialDoesNotExist() {
    final var accountId = UUID.randomUUID();

    final var loaded = target.findByAccountId(accountId);

    assertThat(loaded).isEmpty();
  }

  @Test
  void shouldUpdateVaultKeyMaterial_whenExistingRecord() {
    final var now = Instant.now();
    final var accountId = insertAuthAccount();

    final var initial =
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

    target.save(initial);

    final var updated =
        VaultKeyMaterial.create(
            accountId,
            new byte[] {9},
            new byte[] {2},
            "ARGON2ID",
            new byte[] {3},
            65536,
            3,
            1,
            32,
            "v2",
            now.plusSeconds(10));

    target.update(updated);

    final var loaded = target.findByAccountId(accountId);

    assertThat(loaded).isPresent();
    assertThat(loaded.get())
        .extracting(VaultKeyMaterial::getAccountId, VaultKeyMaterial::getCryptoVersion)
        .containsExactly(accountId, "v2");
  }

  private UUID insertAuthAccount() {
    final var accountId = UUID.randomUUID();
    final var email = "%s@safecube.io".formatted(accountId);
    final var now = Instant.now();

    final var authAccountJpaEntity =
        new AuthAccountJpaEntity(accountId, email, "password", true, now, null);
    authAccountJpaRepository.save(authAccountJpaEntity);

    return accountId;
  }
}
