package com.example.demo.conf;

import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Persists users directly via the repository (bypassing the public API, which only ever creates
 * CLIENT users) so integration tests can exercise MANAGER/EMPLOYEE-only endpoints, and issues a
 * matching JWT via the real {@link JwtService}.
 */
@Component
public class TestUserFactory {

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;

  public record TestUser(User user, String rawPassword, String token) {
    public String authHeader() {
      return "Bearer " + token;
    }
  }

  public TestUser create(UserRole role) {
    return create(role, "password123");
  }

  public TestUser create(UserRole role, String rawPassword) {
    User user =
        User.builder()
            .firstName("Test")
            .lastName(role.name())
            .birthdate(LocalDate.of(1990, 1, 1))
            .email(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.local")
            .password(passwordEncoder.encode(rawPassword))
            .phone("0000000000")
            .role(role)
            .build();
    user = userRepository.save(user);
    String token = jwtService.generateToken(user.getId(), user.getEmail(), role.name());
    return new TestUser(user, rawPassword, token);
  }
}
