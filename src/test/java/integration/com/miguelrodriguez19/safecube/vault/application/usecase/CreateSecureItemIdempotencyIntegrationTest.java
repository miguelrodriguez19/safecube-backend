package integration.com.miguelrodriguez19.safecube.vault.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.command.CreateSecureItemCommand;
import com.miguelrodriguez19.safecube.vault.application.usecase.secureitem.CreateSecureItemUseCase;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemMutationJpaRepository;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest(profiles = {"jpa"})
class CreateSecureItemIdempotencyIntegrationTest {

  @Autowired private AuthAccountJpaRepository authAccountJpaRepository;
  @Autowired private SecureItemJpaRepository secureItemJpaRepository;
  @Autowired private SecureItemMutationJpaRepository mutationJpaRepository;
  @Autowired private CreateSecureItemUseCase target;

  @Test
  void shouldExecuteConcurrentIdenticalCreateOnlyOnce() throws Exception {
    final var accountId = insertAuthAccount();
    final var mutationId = UUID.randomUUID();
    final var firstReceivedAt = Instant.now();
    final var start = new CountDownLatch(1);
    final var executor = Executors.newFixedThreadPool(2);

    try {
      final var first =
          executor.submit(
              () -> {
                start.await();
                return target.execute(command(accountId, mutationId, firstReceivedAt));
              });
      final var retry =
          executor.submit(
              () -> {
                start.await();
                return target.execute(
                    command(accountId, mutationId, firstReceivedAt.plusSeconds(1)));
              });

      start.countDown();
      final var firstResult = first.get().success().orElseThrow();
      final var retryResult = retry.get().success().orElseThrow();

      assertThat(retryResult).isEqualTo(firstResult);
      assertThat(
              secureItemJpaRepository.findAll().stream()
                  .filter(item -> item.getAccountId().equals(accountId)))
          .hasSize(1);
      assertThat(mutationJpaRepository.findByIdAccountIdAndIdMutationId(accountId, mutationId))
          .isPresent();
    } finally {
      executor.shutdownNow();
    }
  }

  private CreateSecureItemCommand command(
      final UUID accountId, final UUID mutationId, final Instant receivedAt) {
    return new CreateSecureItemCommand(
        accountId,
        ItemTypeDto.NOTE,
        1,
        "Concurrent create",
        "encrypted-payload".getBytes(),
        1L,
        mutationId,
        receivedAt);
  }

  private UUID insertAuthAccount() {
    final var accountId = UUID.randomUUID();
    authAccountJpaRepository.save(
        new AuthAccountJpaEntity(
            accountId,
            "%s@safecube.io".formatted(accountId),
            "password",
            true,
            Instant.now(),
            null));
    return accountId;
  }
}
