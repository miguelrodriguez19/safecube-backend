package unit.com.miguelrodriguez19.safecube.vault.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter;
import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.mapper.ListSecureItemsFilterMapper;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ListSecureItemsFilterMapperTest {

  private final ListSecureItemsFilterMapper target = new ListSecureItemsFilterMapper();

  private static Stream<Arguments> paramsCombinations() {
    return Stream.of(
        Arguments.of(Instant.now(), null, null, null, false, null, null),
        Arguments.of(null, Instant.now(), null, null, false, null, null),
        Arguments.of(null, null, ItemTypeDto.NOTE.name(), null, false, null, null),
        Arguments.of(null, null, null, Set.of("AAA"), false, null, null),
        Arguments.of(null, null, null, null, false, 100, null),
        Arguments.of(null, null, null, null, false, null, Order.UPDATED_AT_DESC.name()),
        Arguments.of(
            Instant.now(),
            Instant.now(),
            ItemTypeDto.NOTE.name(),
            Set.of("AAA"),
            true,
            100,
            Order.UPDATED_AT_DESC.name()),
        Arguments.of(null, null, null, null, false, null, null));
  }

  @ParameterizedTest
  @MethodSource("paramsCombinations")
  void shouldMap_givenParams(
      final Instant createdAfter,
      final Instant updatedAfter,
      final String type,
      final Set<String> labels,
      final boolean includeDeleted,
      final Integer limit,
      final String order) {

    final var result =
        target.from(createdAfter, updatedAfter, type, labels, includeDeleted, limit, order);

    assertThat(result)
        .extracting(
            ListSecureItemsFilter::createdAfter,
            ListSecureItemsFilter::updatedAfter,
            ListSecureItemsFilter::type,
            ListSecureItemsFilter::labels,
            ListSecureItemsFilter::includeDeleted,
            ListSecureItemsFilter::limit,
            ListSecureItemsFilter::order)
        .containsExactly(
            createdAfter,
            updatedAfter,
            type != null ? ItemTypeDto.valueOf(type) : null,
            labels,
            includeDeleted,
            limit,
            order != null ? Order.valueOf(order) : null);
  }
}
