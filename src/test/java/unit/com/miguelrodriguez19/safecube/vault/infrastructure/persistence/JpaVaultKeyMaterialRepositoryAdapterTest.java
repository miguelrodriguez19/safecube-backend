package unit.com.miguelrodriguez19.safecube.vault.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.vault.domain.model.keymaterial.VaultKeyMaterial;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.JpaVaultKeyMaterialRepositoryAdapter;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaEntity;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.jpa.VaultKeyMaterialJpaRepository;
import com.miguelrodriguez19.safecube.vault.infrastructure.persistence.mapper.VaultKeyMaterialMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class JpaVaultKeyMaterialRepositoryAdapterTest {

  @Mock private VaultKeyMaterialJpaRepository jpaRepository;
  @Mock private VaultKeyMaterialMapper mapper;

  @InjectMocks private JpaVaultKeyMaterialRepositoryAdapter target;

  @Test
  void shouldReturnDomain_whenEntityExists() {
    final var accountId = UUID.randomUUID();
    final var entity = mock(VaultKeyMaterialJpaEntity.class);
    final var domain = mock(VaultKeyMaterial.class);

    when(jpaRepository.findById(accountId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    final var result = target.findByAccountId(accountId);

    assertThat(result).isPresent();
    assertThat(result.get()).isSameAs(domain);

    verify(jpaRepository).findById(accountId);
    verify(mapper).toDomain(entity);
  }

  @Test
  void shouldReturnEmpty_whenEntityDoesNotExist() {
    final var accountId = UUID.randomUUID();

    when(jpaRepository.findById(accountId)).thenReturn(Optional.empty());

    final var result = target.findByAccountId(accountId);

    assertThat(result).isEmpty();

    verify(jpaRepository).findById(accountId);
    verifyNoInteractions(mapper);
  }

  @Test
  void shouldSaveMappedEntity_whenSavingDomain() {
    final var domain = mock(VaultKeyMaterial.class);
    final var entity = mock(VaultKeyMaterialJpaEntity.class);

    when(mapper.toEntity(domain)).thenReturn(entity);

    target.save(domain);

    verify(mapper).toEntity(domain);
    verify(jpaRepository).save(entity);
  }

  @Test
  void shouldSaveMappedEntity_whenUpdatingDomain() {
    final var domain = mock(VaultKeyMaterial.class);
    final var entity = mock(VaultKeyMaterialJpaEntity.class);

    when(mapper.toEntity(domain)).thenReturn(entity);

    target.update(domain);

    verify(mapper).toEntity(domain);
    verify(jpaRepository).save(entity);
  }
}
