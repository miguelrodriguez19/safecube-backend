package com.miguelrodriguez19.safecube.user.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserProfileJpaEntity
 *
 * <p>JPA entity representing a persisted user profile.
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "user_profiles",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_user_profiles_account_id", columnNames = "account_id")
    })
public class UserProfileJpaEntity {

  @Id private UUID userId;

  @Column(nullable = false)
  private UUID accountId;

  @Column(nullable = false, length = 100)
  private String displayName;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  private Instant deletedAt;
}
