package integration.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.ItemType;
import com.miguelrodriguez19.safecube.vault.domain.model.secureitem.SecureItem;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaSecureItemRepositoryAdapter;
import integration.annotation.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest(profiles = {"jpa"})
class JpaSecureItemRepositoryAdapterIntegrationTest {

  @Autowired private AuthAccountJpaRepository authAccountJpaRepository;

  @Autowired private JpaSecureItemRepositoryAdapter target;

  @Autowired private PlatformTransactionManager transactionManager;

  @Test
  void shouldPersistAndRetrieveSecureItemByIdAndAccount() {
    final var now = Instant.now();
    final var accountId = insertAuthAccount();
    final var itemId = UUID.randomUUID();

    final var secureItem =
        SecureItem.of(itemId, accountId, ItemType.PASSWORD, 1, "GitHub", "payload".getBytes(), now);

    target.save(secureItem);

    final var loaded = target.findByIdAndAccount(itemId, accountId);

    assertThat(loaded).isNotNull();
    assertThat(loaded)
        .extracting(
            SecureItem::getItemId,
            SecureItem::getAccountId,
            SecureItem::getItemType,
            SecureItem::getSchemaVersion,
            SecureItem::getDisplayHint,
            SecureItem::getPayloadVersion)
        .containsExactly(itemId, accountId, ItemType.PASSWORD, 1, "GitHub", 1L);
  }

  @Test
  void shouldReturnNull_whenSecureItemDoesNotExist() {
    final var result = target.findByIdAndAccount(UUID.randomUUID(), UUID.randomUUID());
    assertThat(result).isNull();
  }

  @Test
  void shouldListAllItemsByAccount_includingDeleted() {
    final var now = Instant.now();
    final var accountId = insertAuthAccount();
    final var filters =
        new ListSecureItemsFilter(null, null, null, Set.of(), false, 100, Order.DISPLAY_NAME_ASC);

    final var first =
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.PASSWORD,
            1,
            "GitHub",
            "payload-1".getBytes(),
            1L,
            1L,
            now);

    final var second =
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "Note",
            "payload-2".getBytes(),
            1L,
            2L,
            now.plusSeconds(5));

    target.save(first);
    target.save(second);

    final var listed = target.findFilteredByAccount(accountId, filters);

    assertThat(listed).hasSize(2);
    assertThat(listed)
        .extracting(SecureItem::getItemId)
        .containsExactlyInAnyOrder(first.getItemId(), second.getItemId());
  }

  @Test
  void shouldSoftDeleteSecureItem() {
    final var now = Instant.now();
    final var accountId = insertAuthAccount();
    final var itemId = UUID.randomUUID();

    final var secureItem =
        SecureItem.of(itemId, accountId, ItemType.NOTE, 1, "ToDelete", "payload".getBytes(), now);

    target.save(secureItem);

    final var deletedAt = now.plusSeconds(30).truncatedTo(ChronoUnit.MICROS);
    target.softDeleteIfRevisionMatches(itemId, accountId, 1L, 2L, 2L, deletedAt);

    final var loaded = target.findByIdAndAccount(itemId, accountId);

    assertThat(loaded).isNotNull();
    assertThat(loaded.getDeletedAt()).isEqualTo(deletedAt);
    assertThat(loaded.getItemRevision()).isEqualTo(2L);
  }

  @Test
  void shouldAcceptExactlyOneOfTwoUpdatesUsingTheSameBaseRevision() {
    final var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    final var accountId = insertAuthAccount();
    final var itemId = UUID.randomUUID();
    target.save(
        SecureItem.of(
            itemId, accountId, ItemType.NOTE, 1, "Base", "base".getBytes(), 3L, 10L, now));
    final var first =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "First",
            "first".getBytes(),
            4L,
            2L,
            11L,
            now,
            now.plusSeconds(1),
            null);
    final var second =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "Second",
            "second".getBytes(),
            9L,
            2L,
            12L,
            now,
            now.plusSeconds(2),
            null);

    final var start = new CountDownLatch(1);
    final var executor = Executors.newFixedThreadPool(2);

    try {
      final var firstResult =
          executor.submit(
              () -> {
                start.await();
                return target.updateIfRevisionMatches(first, 1L);
              });
      final var secondResult =
          executor.submit(
              () -> {
                start.await();
                return target.updateIfRevisionMatches(second, 1L);
              });

      start.countDown();
      final var accepted = java.util.List.of(firstResult.get(), secondResult.get());
      final var stored = target.findByIdAndAccount(itemId, accountId);

      assertThat(accepted).containsExactlyInAnyOrder(true, false);
      assertThat(stored.getItemRevision()).isEqualTo(2L);
      assertThat(stored.getPayloadVersion()).isIn(4L, 9L);
      assertThat(stored.getChangeSequence()).isIn(11L, 12L);
    } catch (final Exception exception) {
      throw new AssertionError(exception);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void shouldRejectDeleteAfterUpdateAcceptedFromTheSameBaseRevision() {
    final var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    final var accountId = insertAuthAccount();
    final var itemId = UUID.randomUUID();
    target.save(
        SecureItem.of(
            itemId, accountId, ItemType.NOTE, 1, "Base", "base".getBytes(), 2L, 20L, now));
    final var update =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "Updated",
            "updated".getBytes(),
            3L,
            2L,
            21L,
            now,
            now.plusSeconds(1),
            null);

    assertThat(target.updateIfRevisionMatches(update, 1L)).isTrue();
    assertThat(
            target.softDeleteIfRevisionMatches(itemId, accountId, 1L, 2L, 22L, now.plusSeconds(2)))
        .isFalse();
    assertThat(target.findByIdAndAccount(itemId, accountId).isDeleted()).isFalse();
  }

  @Test
  void shouldRejectUpdateAfterDeleteAcceptedFromTheSameBaseRevision() {
    final var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    final var accountId = insertAuthAccount();
    final var itemId = UUID.randomUUID();
    target.save(
        SecureItem.of(
            itemId, accountId, ItemType.NOTE, 1, "Base", "base".getBytes(), 2L, 20L, now));
    final var update =
        SecureItem.restore(
            itemId,
            accountId,
            ItemType.NOTE,
            1,
            "Updated",
            "updated".getBytes(),
            3L,
            2L,
            22L,
            now,
            now.plusSeconds(2),
            null);

    assertThat(
            target.softDeleteIfRevisionMatches(itemId, accountId, 1L, 2L, 21L, now.plusSeconds(1)))
        .isTrue();
    assertThat(target.updateIfRevisionMatches(update, 1L)).isFalse();
    assertThat(target.findByIdAndAccount(itemId, accountId).isDeleted()).isTrue();
  }

  @Test
  void shouldSerializeChangeSequenceAllocationUntilTheEarlierTransactionCommits() throws Exception {
    final var accountId = insertAuthAccount();
    final var firstAllocated = new CountDownLatch(1);
    final var releaseFirstCommit = new CountDownLatch(1);
    final var secondStarted = new CountDownLatch(1);
    final var transactions = new TransactionTemplate(transactionManager);
    final var executor = Executors.newFixedThreadPool(2);

    try {
      final var first =
          executor.submit(
              () ->
                  transactions.execute(
                      status -> {
                        final var sequence = target.nextChangeSequence(accountId);
                        firstAllocated.countDown();
                        await(releaseFirstCommit);
                        return sequence;
                      }));
      final var second =
          executor.submit(
              () -> {
                await(firstAllocated);
                secondStarted.countDown();
                return transactions.execute(status -> target.nextChangeSequence(accountId));
              });

      assertThat(secondStarted.await(5, TimeUnit.SECONDS)).isTrue();
      org.junit.jupiter.api.Assertions.assertThrows(
          TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));

      releaseFirstCommit.countDown();

      assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(1L);
      assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(2L);
    } finally {
      releaseFirstCommit.countDown();
      executor.shutdownNow();
    }
  }

  @Test
  void shouldPageChangesBySequenceWhenTimestampsAreEqual() {
    final var timestamp = Instant.now().truncatedTo(ChronoUnit.MICROS);
    final var accountId = insertAuthAccount();
    target.save(
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "First",
            "one".getBytes(),
            1L,
            101L,
            timestamp));
    target.save(
        SecureItem.of(
            UUID.randomUUID(),
            accountId,
            ItemType.NOTE,
            1,
            "Second",
            "two".getBytes(),
            1L,
            102L,
            timestamp));

    final var firstPage = target.findChanges(accountId, 100L, 1);
    final var secondPage = target.findChanges(accountId, 101L, 1);

    assertThat(firstPage).extracting(SecureItem::getChangeSequence).containsExactly(101L);
    assertThat(secondPage).extracting(SecureItem::getChangeSequence).containsExactly(102L);
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

  private void await(final CountDownLatch latch) {
    try {
      latch.await();
    } catch (final InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while coordinating concurrent test", exception);
    }
  }
}
