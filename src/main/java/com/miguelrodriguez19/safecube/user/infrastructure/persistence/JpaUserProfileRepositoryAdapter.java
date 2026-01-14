package com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import com.miguelrodriguez19.safecube.user.application.port.out.UserProfileRepository;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaRepository;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.mapper.UserProfileMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * JpaUserProfileRepositoryAdapter
 *
 * <p>JPA-backed implementation of {@link UserProfileRepository}.
 */
@Repository
@RequiredArgsConstructor
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

  private final UserProfileJpaRepository jpaRepository;
  private final UserProfileMapper mapper;

  @Override
  public Optional<UserProfile> findByAccountId(final UUID accountId) {
    return jpaRepository.findByAccountId(accountId).map(mapper::toDomain);
  }

  @Override
  public void save(final UserProfile profile) {
    final var entity = mapper.toEntity(profile);
    jpaRepository.save(entity);
  }
}
