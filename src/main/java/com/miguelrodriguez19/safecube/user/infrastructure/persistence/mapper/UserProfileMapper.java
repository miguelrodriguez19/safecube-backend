package com.miguelrodriguez19.safecube.user.infrastructure.persistence.mapper;

import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa.UserProfileJpaEntity;
import org.springframework.stereotype.Component;

/**
 * UserProfileMapper
 *
 * <p>Maps between {@link UserProfile} domain objects and {@link UserProfileJpaEntity}.
 */
@Component
public class UserProfileMapper {

  public UserProfileJpaEntity toEntity(final UserProfile profile) {
    return new UserProfileJpaEntity(
        profile.getUserId(),
        profile.getAccountId(),
        profile.getDisplayName(),
        profile.getCreatedAt(),
        profile.getUpdatedAt(),
        profile.getDeletedAt());
  }

  public UserProfile toDomain(final UserProfileJpaEntity entity) {
    return UserProfile.restore(
        entity.getUserId(),
        entity.getAccountId(),
        entity.getDisplayName(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt());
  }
}
