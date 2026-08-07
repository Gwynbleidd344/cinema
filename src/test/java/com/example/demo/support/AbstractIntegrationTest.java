ackage com.example.demo.support;

import com.example.demo.conf.FacadeIT;
import com.example.demo.endpoint.rest.controller.auth.dto.AuthResponse;
import com.example.demo.endpoint.rest.controller.auth.dto.LoginRequest;
import com.example.demo.entity.Movie;
import com.example.demo.entity.Projection;
import com.example.demo.entity.Room;
import com.example.demo.entity.Seat;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.Genre;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Shared setup for controller integration tests: a running app (random port) backed by a
 * Testcontainers Postgres instance (via {@link FacadeIT}), plus helpers to create users of any
 * role, log them in, and seed movie/room/seat/projection fixtures.
 */
public abstract class AbstractIntegrationTest extends FacadeIT {

  protected static final String RAW_PASSWORD = "Password123!";

  @Autowired protected TestRestTemplate restTemplate;

  @Autowired protected UserRepository userRepository;
  @Autowired protected MovieRepository movieRepository;
  @Autowired protected RoomRepository roomRepository;
  @Autowired protected SeatRepository seatRepository;
  @Autowired protected ProjectionRepository projectionRepository;
  @Autowired protected ReservationRepository reservationRepository;
  @Autowired protected PasswordEncoder passwordEncoder;

  protected User createUser(UserRole role) {
    return createUser(role, "user-" + UUID.randomUUID() + "@example.com");
  }

  protected User createUser(UserRole role, String email) {
    User user =
        User.builder()
            .firstName("Test")
            .lastName("User")
            .birthdate(LocalDate.of(1990, 1, 1))
            .email(email)
            .phone("0340000000")
            .password(passwordEncoder.encode(RAW_PASSWORD))
            .role(role)
            .build();
    return userRepository.save(user);
  }

  protected String loginAndGetToken(String email) {
    LoginRequest request = new LoginRequest(email, RAW_PASSWORD);
    ResponseEntity<AuthResponse> response =
        restTemplate.postForEntity("/api/v1/auth/login", request, AuthResponse.class);
    return response.getBody().token();
  }

  protected HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  protected Room createRoomWithSeats(int seatCount) {
    Room room = roomRepository.save(Room.builder().number("R-" + UUID.randomUUID()).capacity(seatCount).build());
    for (int i = 0; i < seatCount; i++) {
      seatRepository.save(Seat.builder().number("S" + i).room(room).build());
    }
    return roomRepository.findById(room.getId()).orElseThrow();
  }

  protected Movie createMovie() {
    return movieRepository.save(
        Movie.builder()
            .title("Movie " + UUID.randomUUID())
            .genres(Set.of(Genre.ACTION))
            .description("A test movie")
            .duration(Duration.ofHours(2))
            .build());
  }

  protected Projection createProjection(Movie movie, Room room) {
    return projectionRepository.save(
        Projection.builder()
            .dateTime(Instant.now().plus(1, ChronoUnit.DAYS))
            .seatPrice(new BigDecimal("10.00"))
            .movie(movie)
            .room(room)
            .build());
  }
}


================================================
FILE: src/test/java/com/example/demo/endpoint/rest/controller/auth/AuthControllerIT.java
================================================
package com.example.demo.endpoint.rest.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.endpoint.rest.controller.auth.dto.AuthResponse;
import com.example.demo.endpoint.rest.controller.auth.dto.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerIT extends AbstractIntegrationTest {

  @Test
  void login_with_correct_credentials_returns_token() {
    User user = createUser(UserRole.CLIENT);

    LoginRequest request = new LoginRequest(user.getEmail(), RAW_PASSWORD);
    ResponseEntity<AuthResponse> response =
        restTemplate.postForEntity("/api/v1/auth/login", request, AuthResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(response.getBody().userId()).isEqualTo(user.getId());
    assertThat(response.getBody().role()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  void login_with_wrong_password_is_unauthorized() {
    User user = createUser(UserRole.CLIENT);

    LoginRequest request = new LoginRequest(user.getEmail(), "totally-wrong");
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/auth/login", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void login_with_unknown_email_is_unauthorized() {
    LoginRequest request = new LoginRequest("nobody-" + java.util.UUID.randomUUID() + "@example.com", RAW_PASSWORD);
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/auth/login", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
