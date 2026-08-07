package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.CinemaFixtures;
import com.example.demo.conf.FacadeIT;
import com.example.demo.conf.TestUserFactory;
import com.example.demo.endpoint.rest.controller.dto.UpsertSeatRequest;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.SeatModel;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SeatControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TestUserFactory testUserFactory;
  @Autowired private CinemaFixtures cinemaFixtures;

  @Test
  void anyone_can_list_seats_optionally_filtered_by_room() {
    var fixture = cinemaFixtures.create();

    ResponseEntity<SeatModel[]> response =
        restTemplate.getForEntity(
            "/api/v1/seats?roomId=" + fixture.room().getId(), SeatModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .extracting(SeatModel::id)
        .contains(fixture.seatA().getId(), fixture.seatB().getId());
  }

  @Test
  void anyone_can_get_a_seat_by_id() {
    var fixture = cinemaFixtures.create();

    ResponseEntity<SeatModel> response =
        restTemplate.getForEntity("/api/v1/seats/" + fixture.seatA().getId(), SeatModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().roomId()).isEqualTo(fixture.room().getId());
  }

  @Test
  void getting_unknown_seat_returns_404() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/v1/seats/" + UUID.randomUUID(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void manager_can_create_seat() {
    var fixture = cinemaFixtures.create();
    var manager = testUserFactory.create(UserRole.MANAGER);

    HttpEntity<UpsertSeatRequest> entity =
        new HttpEntity<>(
            new UpsertSeatRequest(null, "Z9", fixture.room().getId()),
            authHeaders(manager.token()));

    ResponseEntity<SeatModel> response =
        restTemplate.exchange("/api/v1/seats", HttpMethod.PUT, entity, SeatModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().number()).isEqualTo("Z9");
  }

  @Test
  void client_cannot_create_seat() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);

    HttpEntity<UpsertSeatRequest> entity =
        new HttpEntity<>(
            new UpsertSeatRequest(null, "Z9", fixture.room().getId()), authHeaders(client.token()));

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/seats", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void creating_seat_for_unknown_room_returns_404() {
    var manager = testUserFactory.create(UserRole.MANAGER);

    HttpEntity<UpsertSeatRequest> entity =
        new HttpEntity<>(
            new UpsertSeatRequest(null, "Z9", UUID.randomUUID()), authHeaders(manager.token()));

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/seats", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return headers;
  }
}
