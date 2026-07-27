package com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import com.miguelrodriguez19.safecube.vault.application.port.out.SecureItemMutationRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemMutationId;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemMutationJpaEntity;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemMutationJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSecureItemMutationRepositoryAdapter implements SecureItemMutationRepository {

  private final SecureItemMutationJpaRepository repository;

  @Override
  public void lock(final UUID accountId, final UUID mutationId) {
    repository.acquireTransactionLock(accountId, mutationId);
  }

  @Override
  public StoredMutation findByAccountAndMutationId(final UUID accountId, final UUID mutationId) {
    return repository
        .findByIdAccountIdAndIdMutationId(accountId, mutationId)
        .map(this::toDomain)
        .orElse(null);
  }

  @Override
  public void save(final StoredMutation mutation) {
    repository.save(
        new SecureItemMutationJpaEntity(
            new SecureItemMutationId(mutation.accountId(), mutation.mutationId()),
            mutation.itemId(),
            mutation.operation(),
            mutation.requestHash(),
            mutation.payloadVersion(),
            mutation.itemRevision(),
            mutation.changeSequence(),
            mutation.occurredAt(),
            mutation.deletedAt()));
  }

  private StoredMutation toDomain(final SecureItemMutationJpaEntity entity) {
    return new StoredMutation(
        entity.getId().getAccountId(),
        entity.getId().getMutationId(),
        entity.getItemId(),
        entity.getOperation(),
        entity.getRequestHash(),
        entity.getPayloadVersion(),
        entity.getItemRevision(),
        entity.getChangeSequence(),
        entity.getOccurredAt(),
        entity.getDeletedAt());
  }
}
