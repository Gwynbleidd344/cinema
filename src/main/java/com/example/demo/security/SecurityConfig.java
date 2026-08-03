package com.example.demo.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless, JWT-based security. Path/role rules below cover everything from doc/api.yml that can
 * be expressed statically. Rules that depend on *who owns a resource* (GET /reservations/{id},
 * GET/PUT /users/{id}, "only EMPLOYEE can validate a reservation") can't be expressed as a path
 * matcher — those are left as "authenticated()" here and must be enforced in the controller/service
 * using {@link AuthenticatedUser}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ping", "/health/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/movies/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/movies")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/movies")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/**")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/rooms/**")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/rooms")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/seats/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/seats")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/projections/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/projections")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reservations")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(HttpMethod.POST, "/api/v1/reservations")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/reservations/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/reservation")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/users")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/users/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
