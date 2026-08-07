package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.CinemaFixtures;
import com.example.demo.conf.FacadeIT;
import com.example.demo.conf.TestUserFactory;
import com.example.demo.endpoint.rest.controller.dto.CreateReservationRequest;
import com.example.demo.endpoint.rest.controller.dto.UpdateReservationRequest;
import com.example.demo.entity.enums.ReservationStatus;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.ReservationModel;
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

class ReservationControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TestUserFactory testUserFactory;
  @Autowired private CinemaFixtures cinemaFixtures;

  @Test
  void client_can_create_a_reservation_for_themselves() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<ReservationModel> response =
        createReservation(
            client.token(),
            fixture.projection().getId(),
            Set.of(fixture.seatA().getId()),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(response.getBody().createdById()).isEqualTo(client.user().getId());
    assertThat(response.getBody().seatIds()).containsExactly(fixture.seatA().getId());
  }

  @Test
  void creating_reservation_with_empty_seats_returns_400() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response =
        createReservation(client.token(), fixture.projection().getId(), Set.of(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void creating_reservation_with_seat_from_another_room_returns_400() {
    var fixture = cinemaFixtures.create();
    var foreignSeat = cinemaFixtures.foreignSeat();
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response =
        createReservation(
            client.token(),
            fixture.projection().getId(),
            Set.of(foreignSeat.getId()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void creating_reservation_with_unknown_seat_returns_404() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response =
        createReservation(
            client.token(), fixture.projection().getId(), Set.of(UUID.randomUUID()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void creating_reservation_with_already_taken_seat_returns_409() {
    var fixture = cinemaFixtures.create();
    var firstClient = testUserFactory.create(UserRole.CLIENT);
    var secondClient = testUserFactory.create(UserRole.CLIENT);

    createReservation(
        firstClient.token(),
        fixture.projection().getId(),
        Set.of(fixture.seatA().getId()),
        ReservationModel.class);

    ResponseEntity<String> response =
        createReservation(
            secondClient.token(),
            fixture.projection().getId(),
            Set.of(fixture.seatA().getId()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void owner_can_get_their_own_reservation() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);
    ReservationModel created =
        createReservation(
                client.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + created.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(client.token())),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id()).isEqualTo(created.id());
  }

  @Test
  void another_client_cannot_get_someone_elses_reservation() {
    var fixture = cinemaFixtures.create();
    var owner = testUserFactory.create(UserRole.CLIENT);
    var otherClient = testUserFactory.create(UserRole.CLIENT);
    ReservationModel created =
        createReservation(
                owner.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + created.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(otherClient.token())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_get_any_reservation() {
    var fixture = cinemaFixtures.create();
    var owner = testUserFactory.create(UserRole.CLIENT);
    var employee = testUserFactory.create(UserRole.EMPLOYEE);
    ReservationModel created =
        createReservation(
                owner.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + created.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(employee.token())),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void client_cannot_list_all_reservations() {
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservations",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(client.token())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_list_all_reservations() {
    var fixture = cinemaFixtures.create();
    var owner = testUserFactory.create(UserRole.CLIENT);
    var manager = testUserFactory.create(UserRole.MANAGER);
    createReservation(
        owner.token(),
        fixture.projection().getId(),
        Set.of(fixture.seatA().getId()),
        ReservationModel.class);

    ResponseEntity<ReservationModel[]> response =
        restTemplate.exchange(
            "/api/v1/reservations",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(manager.token())),
            ReservationModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void owner_can_change_seats_without_validating() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);
    ReservationModel created =
        createReservation(
                client.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    HttpEntity<UpdateReservationRequest> entity =
        new HttpEntity<>(
            new UpdateReservationRequest(created.id(), null, Set.of(fixture.seatB().getId())),
            authHeaders(client.token()));

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservation", HttpMethod.PUT, entity, ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().seatIds()).containsExactly(fixture.seatB().getId());
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.PENDING);
  }

  @Test
  void client_cannot_validate_their_own_reservation() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);
    ReservationModel created =
        createReservation(
                client.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    HttpEntity<UpdateReservationRequest> entity =
        new HttpEntity<>(
            new UpdateReservationRequest(created.id(), ReservationStatus.SUCCESS, null),
            authHeaders(client.token()));

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/reservation", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_validate_a_reservation() {
    var fixture = cinemaFixtures.create();
    var client = testUserFactory.create(UserRole.CLIENT);
    var employee = testUserFactory.create(UserRole.EMPLOYEE);
    ReservationModel created =
        createReservation(
                client.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    HttpEntity<UpdateReservationRequest> entity =
        new HttpEntity<>(
            new UpdateReservationRequest(created.id(), ReservationStatus.SUCCESS, null),
            authHeaders(employee.token()));

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservation", HttpMethod.PUT, entity, ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
    assertThat(response.getBody().processedById()).isEqualTo(employee.user().getId());
  }

  @Test
  void other_client_cannot_update_someone_elses_reservation() {
    var fixture = cinemaFixtures.create();
    var owner = testUserFactory.create(UserRole.CLIENT);
    var otherClient = testUserFactory.create(UserRole.CLIENT);
    ReservationModel created =
        createReservation(
                owner.token(),
                fixture.projection().getId(),
                Set.of(fixture.seatA().getId()),
                ReservationModel.class)
            .getBody();

    HttpEntity<UpdateReservationRequest> entity =
        new HttpEntity<>(
            new UpdateReservationRequest(created.id(), null, Set.of(fixture.seatB().getId())),
            authHeaders(otherClient.token()));

    ResponseEntity<String> response =
        restTemplate.exchange("/api/v1/reservation", HttpMethod.PUT, entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private <T> ResponseEntity<T> createReservation(
      String token, UUID projectionId, Set<UUID> seatIds, Class<T> responseType) {
    HttpEntity<CreateReservationRequest> entity =
        new HttpEntity<>(new CreateReservationRequest(projectionId, seatIds), authHeaders(token));
    return restTemplate.postForEntity("/api/v1/reservations", entity, responseType);
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return headers;
  }
}
