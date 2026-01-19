package unit.com.miguelrodriguez19.safecube.vault.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.miguelrodriguez19.safecube.vault.application.dto.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.mapper.ItemTypeMapper;
import com.miguelrodriguez19.safecube.vault.domain.model.ItemType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import unit.annotation.UnitTest;

@UnitTest
class ItemTypeMapperTest {

  private final ItemTypeMapper target = new ItemTypeMapper();

  private static Stream<ItemType> allItemTypeValues() {
    return Arrays.stream(ItemType.values());
  }

  @ParameterizedTest
  @MethodSource("allItemTypeValues")
  void shouldMapAllEnumValuesToDto(final ItemType type) {
    ItemTypeDto dto = target.toDto(type);
    assertThat(type.name()).isEqualTo(dto.name());
  }

  private static Stream<ItemTypeDto> allItemTypeDtoValues() {
    return Arrays.stream(ItemTypeDto.values());
  }

  @ParameterizedTest
  @MethodSource("allItemTypeDtoValues")
  void shouldMapAllEnumValuesToDomain(final ItemTypeDto dto) {
    final var type = target.toDomain(dto);
    assertThat(dto.name()).isEqualTo(type.name());
  }

  @Test
  void shouldThrowException_givenInvalidItemType() {
    assertThatCode(() -> target.toDto(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowException_givenInvalidItemTypeDto() {
    assertThatCode(() -> target.toDomain(null)).isInstanceOf(NullPointerException.class);
  }
}
