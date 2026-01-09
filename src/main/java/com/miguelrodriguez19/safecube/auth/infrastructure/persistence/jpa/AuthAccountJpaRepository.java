package com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAccountJpaRepository extends JpaRepository<AuthAccountJpaEntity, UUID> {

  boolean existsByEmail(String email);

  Optional<AuthAccountJpaEntity> findByEmail(String email);
}
