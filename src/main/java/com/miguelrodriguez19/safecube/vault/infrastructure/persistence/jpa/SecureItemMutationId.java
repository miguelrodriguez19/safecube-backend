package com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SecureItemMutationId implements Serializable {

  @Column(nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private UUID mutationId;
}
