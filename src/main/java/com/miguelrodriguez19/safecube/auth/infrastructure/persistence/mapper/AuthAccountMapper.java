package com.miguelrodriguez19.safecube.auth.infrastructure.persistence.mapper;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa.AuthAccountJpaEntity;
import org.springframework.stereotype.Component;

/**
 * AuthAccountMapper
 *
 * <p>Maps between JPA entities and domain models.
 */
@Component
public final class AuthAccountMapper {

  public AuthAccount toDomain(final AuthAccountJpaEntity entity) {
    return AuthAccount.restore(
        entity.getAccountId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.isEnabled(),
        entity.getCreatedAt(),
        entity.getDisabledAt());
  }

  public AuthAccountJpaEntity toEntity(final AuthAccount account) {
    return new AuthAccountJpaEntity(
        account.getAccountId(),
        account.getEmail(),
        account.getPasswordHash(),
        account.isEnabled(),
        account.getCreatedAt(),
        account.getDisabledAt());
  }
}
