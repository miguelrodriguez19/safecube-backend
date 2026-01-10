package com.miguelrodriguez19.safecube.auth.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AuthAccountJpaEntity
 *
 * <p>JPA representation of {@link com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount}.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_accounts")
public class AuthAccountJpaEntity {

  @Id private UUID accountId;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant disabledAt;
}
