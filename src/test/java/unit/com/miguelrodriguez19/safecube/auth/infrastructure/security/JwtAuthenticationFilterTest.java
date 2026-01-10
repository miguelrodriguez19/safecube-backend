package unit.com.miguelrodriguez19.safecube.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtAuthenticationFilter;
import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtTokenParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import unit.annotation.UnitTest;

@UnitTest
class JwtAuthenticationFilterTest {

  @AfterEach
  void cleanUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldPopulateSecurityContext_whenTokenIsValid() throws Exception {
    final var accountId = UUID.randomUUID();

    final var tokenParser = Mockito.mock(JwtTokenParser.class);
    when(tokenParser.extractAccountId("valid-token")).thenReturn(Optional.of(accountId));

    final var target = new JwtAuthenticationFilter(tokenParser);

    final var request = Mockito.mock(HttpServletRequest.class);
    final var response = Mockito.mock(HttpServletResponse.class);
    final var filterChain = Mockito.mock(FilterChain.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

    target.doFilter(request, response, filterChain);

    final var authentication = SecurityContextHolder.getContext().getAuthentication();

    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isEqualTo(accountId);
  }

  @Test
  void shouldNotAuthenticate_whenHeaderIsMissing() throws Exception {
    final var tokenParser = Mockito.mock(JwtTokenParser.class);
    final var target = new JwtAuthenticationFilter(tokenParser);

    final var request = Mockito.mock(HttpServletRequest.class);
    final var response = Mockito.mock(HttpServletResponse.class);
    final var filterChain = Mockito.mock(FilterChain.class);

    when(request.getHeader("Authorization")).thenReturn(null);

    target.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
