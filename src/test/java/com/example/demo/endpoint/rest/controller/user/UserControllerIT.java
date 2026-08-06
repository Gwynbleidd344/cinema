package com.example.demo.endpoint.rest.controller.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.endpoint.rest.controller.user.dto.RegisterUserRequest;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.UserModel;
import com.example.demo.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserControllerIT extends AbstractIntegrationTest {

  @Test
  void register_new_user_succeeds_with_client_role() {
    RegisterUserRequest request =
        new RegisterUserRequest(
            "Jane",
            "Doe",
            LocalDate.of(1995, 5, 20),
            "jane-" + UUID.randomUUID() + "@example.com",
            "0341234567",
            "s3cr3t!");

    ResponseEntity<UserModel> response =
        restTemplate.postForEntity("/api/v1/users", request, UserModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().role()).isEqualTo(UserRole.CLIENT);
    assertThat(response.getBody().id()).isNotNull();
  }

  @Test
  void register_with_existing_email_returns_conflict() {
    String email = "dup-" + UUID.randomUUID() + "@example.com";
    RegisterUserRequest request =
        new RegisterUserRequest("Jane", "Doe", LocalDate.of(1995, 5, 20), email, "0341234567", "s3cr3t!");

    restTemplate.postForEntity("/api/v1/users", request, UserModel.class);
    ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/users", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}


================================================
FILE: src/test/java/com/example/demo/endpoint/rest/controller/movie/MovieControllerIT.java
================================================
package com.example.demo.endpoint.rest.controller.movie;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.endpoint.rest.controller.movie.dto.CreateMovieRequest;
import com.example.demo.endpoint.rest.controller.movie.dto.UpsertMovieRequest;
import com.example.demo.entity.Movie;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.Genre;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.MovieModel;
import com.example.demo.support.AbstractIntegrationTest;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MovieControllerIT extends AbstractIntegrationTest {

  @Test
  void list_movies_is_public() {
    createMovie();

    ResponseEntity<MovieModel[]> response = restTemplate.getForEntity("/api/v1/movies", MovieModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void get_movie_by_id_is_public() {
    Movie movie = createMovie();

    ResponseEntity<MovieModel> response =
        restTemplate.getForEntity("/api/v1/movies/" + movie.getId(), MovieModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().title()).isEqualTo(movie.getTitle());
  }

  @Test
  void get_movie_by_id_not_found() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/movies/" + UUID.randomUUID(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_movie_as_manager_succeeds() {
    User manager = createUser(UserRole.MANAGER);
    String token = loginAndGetToken(manager.getEmail());

    CreateMovieRequest request =
        new CreateMovieRequest("Dune", Set.of(Genre.SCI_FI), "desc", Duration.ofMinutes(155));
    HttpEntity<CreateMovieRequest> entity = new HttpEntity<>(request, authHeaders(token));

    ResponseEntity<MovieModel> response =
        restTemplate.postForEntity("/api/v1/movies", entity, MovieModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id()).isNotNull();
  }

  @Test
  void create_movie_as_client_is_forbidden() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());

    CreateMovieRequest request =
        new CreateMovieRequest("Dune", Set.of(Genre.SCI_FI), "desc", Duration.ofMinutes(155));
    HttpEntity<CreateMovieRequest> entity = new HttpEntity<>(request, authHeaders(token));

    ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/movies", entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void upsert_movie_as_manager_succeeds() {
    Movie movie = createMovie();
    User manager = createUser(UserRole.MANAGER);
    String token = loginAndGetToken(manager.getEmail());

    UpsertMovieRequest request =
        new UpsertMovieRequest(
            movie.getId(), "Updated title", movie.getGenres(), movie.getDescription(), movie.getDuration());
    HttpEntity<UpsertMovieRequest> entity = new HttpEntity<>(request, authHeaders(token));

    ResponseEntity<MovieModel> response =
        restTemplate.exchange("/api/v1/movies", HttpMethod.PUT, entity, MovieModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().title()).isEqualTo("Updated title");
  }

  @Test
  void upsert_movie_as_employee_is_forbidden() {
    Movie movie = createMovie();
    User employee = createUser(UserRole.EMPLOYEE);
    String token = loginAndGetToken(employee.getEmail());

    UpsertMovieRequest request =
        new UpsertMovieRequest(
            movie.getId(), "Nope", movie.getGenres(), movie.getDescription(), movie.getDuration());
    HttpEntity<UpsertMovieRequest> entity = new HttpEntity<>(request, authHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/movies", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_movie_as_manager_succeeds() {
    Movie movie = createMovie();
    User manager = createUser(UserRole.MANAGER);
    String token = loginAndGetToken(manager.getEmail());

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/api/v1/movies/" + movie.getId(),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(token)),
            Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void delete_movie_as_client_is_forbidden() {
    Movie movie = createMovie();
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/movies/" + movie.getId(),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_nonexistent_movie_as_manager_returns_not_found() {
    User manager = createUser(UserRole.MANAGER);
    String token = loginAndGetToken(manager.getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/movies/" + UUID.randomUUID(),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
