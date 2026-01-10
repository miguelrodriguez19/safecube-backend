package com.miguelrodriguez19.safecube.auth.infrastructure.persistence;

import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaRepository;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.mapper.AuthAccountMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * JpaAuthAccountRepositoryAdapter
 *
 * <p>JPA-backed implementation of {@link AuthAccountRepository}.
 */
@Repository
@RequiredArgsConstructor
public class JpaAuthAccountRepositoryAdapter implements AuthAccountRepository {

  private final AuthAccountJpaRepository jpaRepository;
  private final AuthAccountMapper mapper;

  @Override
  public boolean existsByEmail(final String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public Optional<AuthAccount> findByEmail(final String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public void save(final AuthAccount account) {
    final var authAccountEntity = mapper.toEntity(account);
    jpaRepository.save(authAccountEntity);
  }
}
