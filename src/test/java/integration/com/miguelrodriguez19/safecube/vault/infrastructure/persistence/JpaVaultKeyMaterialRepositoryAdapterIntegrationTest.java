package integration.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaVaultKeyMaterialRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            VaultKeyMaterial::getCryptoVersion,
            VaultKeyMaterial::getMasterKeyRevision)
        .containsExactly(accountId, "ARGON2ID", "v1", 1L);
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

  @Test
  void shouldUpdateOnlyMasterWrappedKekAndRevision_whenRevisionMatches() {
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

    final var updatedRows =
        target.updateMasterWrappedKekIfRevisionMatches(
            accountId, new byte[] {9, 9}, 1L, now.plusSeconds(10));

    assertThat(updatedRows).isEqualTo(1);

    final var loaded = target.findByAccountId(accountId).orElseThrow();
    assertThat(loaded.getKekEncMaster()).containsExactly(9, 9);
    assertThat(loaded.getKekEncRecovery()).containsExactly(2);
    assertThat(loaded.getKdfAlgorithm()).isEqualTo("ARGON2ID");
    assertThat(loaded.getKdfSalt()).containsExactly(3);
    assertThat(loaded.getKdfMemoryKib()).isEqualTo(65536);
    assertThat(loaded.getKdfIterations()).isEqualTo(3);
    assertThat(loaded.getKdfParallelism()).isEqualTo(1);
    assertThat(loaded.getKdfOutputLen()).isEqualTo(32);
    assertThat(loaded.getCryptoVersion()).isEqualTo("v1");
    assertThat(loaded.getCreatedAt()).isEqualTo(now);
    assertThat(loaded.getUpdatedAt()).isEqualTo(now.plusSeconds(10));
    assertThat(loaded.getMasterKeyRevision()).isEqualTo(2L);

    final var staleRows =
        target.updateMasterWrappedKekIfRevisionMatches(
            accountId, new byte[] {8}, 1L, now.plusSeconds(20));

    assertThat(staleRows).isZero();
    final var afterStale = target.findByAccountId(accountId).orElseThrow();
    assertThat(afterStale.getKekEncMaster()).containsExactly(9, 9);
    assertThat(afterStale.getMasterKeyRevision()).isEqualTo(2L);
    assertThat(afterStale.getUpdatedAt()).isEqualTo(now.plusSeconds(10));
  }

  @Test
  void shouldAllowExactlyOneConcurrentCas_whenBothUseSameRevision() throws Exception {
    final var now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    final var accountId = insertAuthAccount();
    target.save(
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
            now));

    final var ready = new CountDownLatch(2);
    final ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      final Future<Integer> first =
          executor.submit(
              () -> {
                ready.countDown();
                ready.await();
                return target.updateMasterWrappedKekIfRevisionMatches(
                    accountId, new byte[] {8}, 1L, now.plusSeconds(1));
              });
      final Future<Integer> second =
          executor.submit(
              () -> {
                ready.countDown();
                ready.await();
                return target.updateMasterWrappedKekIfRevisionMatches(
                    accountId, new byte[] {9}, 1L, now.plusSeconds(2));
              });

      assertThat(List.of(first.get(), second.get())).containsExactlyInAnyOrder(0, 1);
    } finally {
      executor.shutdownNow();
    }

    final var loaded = target.findByAccountId(accountId).orElseThrow();
    assertThat(loaded.getMasterKeyRevision()).isEqualTo(2L);
    assertThat(loaded.getKekEncMaster()).containsAnyOf((byte) 8, (byte) 9);
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
