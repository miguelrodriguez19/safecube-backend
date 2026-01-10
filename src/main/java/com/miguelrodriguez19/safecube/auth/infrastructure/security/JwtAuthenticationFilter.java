package com.miguelrodriguez19.safecube.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtAuthenticationFilter
 *
 * <p>Authenticates requests using Bearer JWT tokens.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenParser tokenParser;

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final @NonNull HttpServletResponse response,
      final @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final var header = request.getHeader(AUTHORIZATION_HEADER);

    if (header != null && header.startsWith(BEARER)) {
      final var token = header.substring(BEARER.length());

      tokenParser.extractAccountId(token)
          .ifPresent(accountId -> {
            final var authentication =
                new UsernamePasswordAuthenticationToken(
                    accountId,
                    null,
                    List.of());

            SecurityContextHolder.getContext()
                .setAuthentication(authentication);
          });
    }

    filterChain.doFilter(request, response);
  }
}
