package com.miguelrodriguez19.safecube.shared.security;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtAuthenticationFilter;
import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtTokenParser;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * <p>JWT-based security configuration.
 *
 * <p>All endpoints are protected by default, except explicitly whitelisted public endpoints. No
 * session, no form login, no HTTP basic authentication.
 */
@Configuration
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS =
      new String[] {"/auth/register", "/auth/login", "/auth/refresh", "/actuator/health", "/error"};

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(final JwtTokenParser tokenParser) {
    return new JwtAuthenticationFilter(tokenParser);
  }

  @Bean
  public AuthenticationEntryPoint restAuthenticationEntryPoint() {
    return (request, response, authException) ->
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final JwtAuthenticationFilter jwtFilter,
      final AuthenticationEntryPoint authenticationEntryPoint) {

    http.sessionManagement(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return authentication -> {
      throw new UnsupportedOperationException("Authentication is handled via JWT");
    };
  }
}
