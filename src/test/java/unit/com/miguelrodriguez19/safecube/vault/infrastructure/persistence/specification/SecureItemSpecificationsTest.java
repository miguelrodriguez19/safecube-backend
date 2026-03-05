package unit.com.miguelrodriguez19.safecube.vault.infrastructure.persistence.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.SecureItemJpaEntity;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.specification.SecureItemSpecifications;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import unit.annotation.UnitTest;

@UnitTest
class SecureItemSpecificationsTest {

  @Test
  void accountIs_shouldBuildEqualityPredicate() {
    final var accountId = UUID.randomUUID();

    final Root<SecureItemJpaEntity> root = mock(Root.class);
    final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    final Path<Object> path = mock(Path.class);
    final Predicate predicate = mock(Predicate.class);

    when(root.get("accountId")).thenReturn(path);
    when(cb.equal(path, accountId)).thenReturn(predicate);

    final Specification<SecureItemJpaEntity> spec = SecureItemSpecifications.accountIs(accountId);

    final var result = spec.toPredicate(root, query, cb);

    assertThat(result).isSameAs(predicate);
    verify(cb).equal(path, accountId);
  }

  @Test
  void createdAfter_shouldBuildGreaterThanOrEqualToPredicate() {
    final var createdAt = Instant.now();

    final Root<SecureItemJpaEntity> root = mock(Root.class);
    final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);

    @SuppressWarnings("unchecked")
    final Path<Instant> path = mock(Path.class);

    final Predicate predicate = mock(Predicate.class);

    when(root.get("createdAt")).thenReturn((Path) path);
    when(cb.greaterThanOrEqualTo(path, createdAt)).thenReturn(predicate);

    final Specification<SecureItemJpaEntity> spec =
        SecureItemSpecifications.createdAfter(createdAt);

    final var result = spec.toPredicate(root, query, cb);

    assertThat(result).isSameAs(predicate);
    verify(cb).greaterThanOrEqualTo(path, createdAt);
  }

  @Test
  void updatedAfter_shouldBuildGreaterThanOrEqualToPredicate() {
    final var updatedAt = Instant.now();

    final Root<SecureItemJpaEntity> root = mock(Root.class);
    final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);

    @SuppressWarnings("unchecked")
    final Path<Instant> path = mock(Path.class);

    final Predicate predicate = mock(Predicate.class);

    when(root.get("updatedAt")).thenReturn((Path) path);
    when(cb.greaterThanOrEqualTo(path, updatedAt)).thenReturn(predicate);

    final Specification<SecureItemJpaEntity> spec =
        SecureItemSpecifications.updatedAfter(updatedAt);

    final var result = spec.toPredicate(root, query, cb);

    assertThat(result).isSameAs(predicate);
    verify(cb).greaterThanOrEqualTo(path, updatedAt);
  }

  @Test
  void hasType_shouldBuildEqualityPredicate() {
    final var type = "PASSWORD";

    final Root<SecureItemJpaEntity> root = mock(Root.class);
    final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);

    @SuppressWarnings("unchecked")
    final Path<String> path = mock(Path.class);

    final Predicate predicate = mock(Predicate.class);

    when(root.get("itemType")).thenReturn((Path) path);
    when(cb.equal(path, type)).thenReturn(predicate);

    final Specification<SecureItemJpaEntity> spec = SecureItemSpecifications.hasType(type);

    final var result = spec.toPredicate(root, query, cb);

    assertThat(result).isSameAs(predicate);
    verify(cb).equal(path, type);
  }

  @Test
  void notDeleted_shouldBuildIsNullPredicate() {
    final Root<SecureItemJpaEntity> root = mock(Root.class);
    final CriteriaQuery<?> query = mock(CriteriaQuery.class);
    final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    final Path<Object> path = mock(Path.class);
    final Predicate predicate = mock(Predicate.class);

    when(root.get("deletedAt")).thenReturn(path);
    when(cb.isNull(path)).thenReturn(predicate);

    final Specification<SecureItemJpaEntity> spec = SecureItemSpecifications.notDeleted();

    final var result = spec.toPredicate(root, query, cb);

    assertThat(result).isSameAs(predicate);
    verify(cb).isNull(path);
  }
}
