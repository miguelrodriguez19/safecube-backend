package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.specification;

import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class that provides reusable {@link org.springframework.data.jpa.domain.Specification}
 * instances for filtering {@link SecureItemJpaEntity} queries.
 *
 * <p>These specifications are intended to be composed using logical operators such as {@code and()}
 * and {@code or()} to build dynamic and type-safe database queries.
 */
public final class SecureItemSpecifications {

  private SecureItemSpecifications() {}

  public static Specification<SecureItemJpaEntity> accountIs(final UUID accountId) {
    return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
  }

  public static Specification<SecureItemJpaEntity> createdAfter(final Instant date) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), date);
  }

  public static Specification<SecureItemJpaEntity> updatedAfter(final Instant since) {
    return (root, query, cb) -> cb.greaterThan(root.get("updatedAt"), since);
  }

  public static Specification<SecureItemJpaEntity> hasType(final String type) {
    return (root, query, cb) -> cb.equal(root.get("itemType"), type);
  }

  public static Specification<SecureItemJpaEntity> notDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }
}
