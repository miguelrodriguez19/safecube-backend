package com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserProfileJpaRepository
 *
 * <p>Spring Data JPA repository for {@link UserProfileJpaEntity}.
 */
public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {

  Optional<UserProfileJpaEntity> findByAccountId(final UUID accountId);
}
