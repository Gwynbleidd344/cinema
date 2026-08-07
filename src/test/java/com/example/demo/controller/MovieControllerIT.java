package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.conf.TestUserFactory;
import com.example.demo.endpoint.rest.controller.dto.CreateMovieRequest;
import com.example.demo.endpoint.rest.controller.dto.UpsertMovieRequest;
import com.example.demo.entity.enums.Genre;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.MovieModel;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MovieControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TestUserFactory testUserFactory;

  @Test
  void list_movies_is_open_to_everyone() {
    createMovieAsManager("Anonymous Access Movie");

    ResponseEntity<MovieModel[]> response =
        restTemplate.getForEntity("/api/v1/movies", MovieModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(List.of(response.getBody()))
        .anyMatch(m -> "Anonymous Access Movie".equals(m.title()));
  }

  @Test
  void get_unknown_movie_returns_404() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/movies/" + UUID.randomUUID(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void manager_can_create_movie() {
    ResponseEntity<MovieModel> response = createMovieAsManager("Dune");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo("Dune");
    assertThat(response.getBody().id()).isNotNull();
  }

  @Test
  void client_cannot_create_movie() {
    var client = testUserFactory.create(UserRole.CLIENT);
    HttpEntity<CreateMovieRequest> entity =
        new HttpEntity<>(
            new CreateMovieRequest(
                "Forbidden Movie", Set.of(Genre.DRAMA), "desc", Duration.ofMinutes(90)),
            authHeaders(client.token()));

    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/movies", entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_create_movie() {
    var employee = testUserFactory.create(UserRole.EMPLOYEE);
    HttpEntity<CreateMovieRequest> entity =
        new HttpEntity<>(
            new CreateMovieRequest(
                "Forbidden Movie 2", Set.of(Genre.COMEDY), "desc", Duration.ofMinutes(90)),
            authHeaders(employee.token()));

    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/movies", entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_update_movie_via_upsert() {
    MovieModel created = createMovieAsManager("Old Title").getBody();
    var manager = testUserFactory.create(UserRole.MANAGER);

    HttpEntity<UpsertMovieRequest> entity =
        new HttpEntity<>(
            new UpsertMovieRequest(
                created.id(),
                "New Title",
                Set.of(Genre.SCI_FI),
                "updated desc",
                Duration.ofHours(2)),
            authHeaders(manager.token()));

    ResponseEntity<MovieModel> response =
        restTemplate.exchange("/api/v1/movies", HttpMethod.PUT, entity, MovieModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().title()).isEqualTo("New Title");
    assertThat(response.getBody().id()).isEqualTo(created.id());
  }

  @Test
  void manager_can_delete_movie() {
    MovieModel created = createMovieAsManager("To Delete").getBody();
    var manager = testUserFactory.create(UserRole.MANAGER);

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/api/v1/movies/" + created.id(),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(manager.token())),
            Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<String> afterDelete =
        restTemplate.getForEntity("/api/v1/movies/" + created.id(), String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleting_unknown_movie_returns_404() {
    var manager = testUserFactory.create(UserRole.MANAGER);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/movies/" + UUID.randomUUID(),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(manager.token())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private ResponseEntity<MovieModel> createMovieAsManager(String title) {
    var manager = testUserFactory.create(UserRole.MANAGER);
    HttpEntity<CreateMovieRequest> entity =
        new HttpEntity<>(
            new CreateMovieRequest(title, Set.of(Genre.ACTION), "desc", Duration.ofMinutes(120)),
            authHeaders(manager.token()));
    return restTemplate.postForEntity("/api/v1/movies", entity, MovieModel.class);
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return headers;
  }
}
