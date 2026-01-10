package com.miguelrodriguez19.safecube.shared.security;

import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtAuthenticationFilter;
import com.miguelrodriguez19.safecube.auth.infrastructure.security.JwtTokenParser;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * <p>Allows unauthenticated access to auth endpoints and actuator health.
 *
 * <p>All other endpoints require a valid Bearer JWT.
 */
@Configuration
public class SecurityConfig {

  private final String[] publicEndpoints =
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
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http, final JwtAuthenticationFilter jwtFilter) throws Exception {

    http.sessionManagement(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(publicEndpoints).permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
