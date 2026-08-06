package com.example.demo.endpoint.rest.controller.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.endpoint.rest.controller.reservation.dto.CreateReservationRequest;
import com.example.demo.endpoint.rest.controller.reservation.dto.UpdateReservationRequest;
import com.example.demo.entity.Movie;
import com.example.demo.entity.Projection;
import com.example.demo.entity.Room;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.ReservationStatus;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.ReservationModel;
import com.example.demo.support.AbstractIntegrationTest;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ReservationControllerIT extends AbstractIntegrationTest {

  @Test
  void client_can_create_reservation() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());
    Room room = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID seatId = room.getSeats().get(0).getId();

    CreateReservationRequest request = new CreateReservationRequest(projection.getId(), Set.of(seatId));
    ResponseEntity<ReservationModel> response =
        restTemplate.postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token)), ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.PENDING);
  }

  @Test
  void anonymous_cannot_create_reservation() {
    Room room = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID seatId = room.getSeats().get(0).getId();

    CreateReservationRequest request = new CreateReservationRequest(projection.getId(), Set.of(seatId));
    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/v1/reservations", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void create_reservation_with_empty_seats_is_bad_request() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());
    Room room = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);

    CreateReservationRequest request = new CreateReservationRequest(projection.getId(), Set.of());
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_reservation_with_unknown_projection_is_not_found() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());

    CreateReservationRequest request =
        new CreateReservationRequest(UUID.randomUUID(), Set.of(UUID.randomUUID()));
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_reservation_with_seat_from_another_room_is_bad_request() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());
    Room room = createRoomWithSeats(1);
    Room otherRoom = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID otherRoomSeatId = otherRoom.getSeats().get(0).getId();

    CreateReservationRequest request =
        new CreateReservationRequest(projection.getId(), Set.of(otherRoomSeatId));
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void double_booking_same_seat_returns_conflict() {
    User client1 = createUser(UserRole.CLIENT);
    User client2 = createUser(UserRole.CLIENT);
    String token1 = loginAndGetToken(client1.getEmail());
    String token2 = loginAndGetToken(client2.getEmail());
    Room room = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID seatId = room.getSeats().get(0).getId();
    CreateReservationRequest request = new CreateReservationRequest(projection.getId(), Set.of(seatId));

    restTemplate.postForEntity(
        "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token1)), ReservationModel.class);
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(token2)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void owner_can_get_own_reservation() {
    User owner = createUser(UserRole.CLIENT);
    String ownerToken = loginAndGetToken(owner.getEmail());
    ReservationModel reservation = createReservation(ownerToken);

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + reservation.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(ownerToken)),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void other_client_cannot_get_someone_elses_reservation() {
    User owner = createUser(UserRole.CLIENT);
    String ownerToken = loginAndGetToken(owner.getEmail());
    ReservationModel reservation = createReservation(ownerToken);

    User intruder = createUser(UserRole.CLIENT);
    String intruderToken = loginAndGetToken(intruder.getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + reservation.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(intruderToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_get_any_reservation() {
    User owner = createUser(UserRole.CLIENT);
    String ownerToken = loginAndGetToken(owner.getEmail());
    ReservationModel reservation = createReservation(ownerToken);

    User employee = createUser(UserRole.EMPLOYEE);
    String employeeToken = loginAndGetToken(employee.getEmail());

    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservations/" + reservation.id(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(employeeToken)),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void client_cannot_list_all_reservations() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservations", HttpMethod.GET, new HttpEntity<>(authHeaders(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_list_all_reservations() {
    User employee = createUser(UserRole.EMPLOYEE);
    String token = loginAndGetToken(employee.getEmail());

    ResponseEntity<ReservationModel[]> response =
        restTemplate.exchange(
            "/api/v1/reservations",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(token)),
            ReservationModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void owner_can_update_seats_without_validating() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());
    Room room = createRoomWithSeats(2);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID seatId1 = room.getSeats().get(0).getId();
    UUID seatId2 = room.getSeats().get(1).getId();

    CreateReservationRequest createRequest =
        new CreateReservationRequest(projection.getId(), Set.of(seatId1));
    ReservationModel created =
        restTemplate
            .postForEntity(
                "/api/v1/reservations",
                new HttpEntity<>(createRequest, authHeaders(token)),
                ReservationModel.class)
            .getBody();

    UpdateReservationRequest updateRequest =
        new UpdateReservationRequest(created.id(), ReservationStatus.PENDING, Set.of(seatId2));
    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservation",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(token)),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().seatIds()).containsExactly(seatId2);
  }

  @Test
  void client_cannot_validate_own_reservation() {
    User client = createUser(UserRole.CLIENT);
    String token = loginAndGetToken(client.getEmail());
    ReservationModel reservation = createReservation(token);

    UpdateReservationRequest updateRequest =
        new UpdateReservationRequest(reservation.id(), ReservationStatus.SUCCESS, null);
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservation",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_validate_reservation() {
    User client = createUser(UserRole.CLIENT);
    String clientToken = loginAndGetToken(client.getEmail());
    ReservationModel reservation = createReservation(clientToken);

    User employee = createUser(UserRole.EMPLOYEE);
    String employeeToken = loginAndGetToken(employee.getEmail());

    UpdateReservationRequest updateRequest =
        new UpdateReservationRequest(reservation.id(), ReservationStatus.SUCCESS, null);
    ResponseEntity<ReservationModel> response =
        restTemplate.exchange(
            "/api/v1/reservation",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(employeeToken)),
            ReservationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
    assertThat(response.getBody().processedById()).isEqualTo(employee.getId());
  }

  @Test
  void stranger_cannot_update_someone_elses_reservation() {
    User owner = createUser(UserRole.CLIENT);
    String ownerToken = loginAndGetToken(owner.getEmail());
    ReservationModel reservation = createReservation(ownerToken);

    User stranger = createUser(UserRole.CLIENT);
    String strangerToken = loginAndGetToken(stranger.getEmail());

    UpdateReservationRequest updateRequest =
        new UpdateReservationRequest(reservation.id(), ReservationStatus.PENDING, null);
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/reservation",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, authHeaders(strangerToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private ReservationModel createReservation(String ownerToken) {
    Room room = createRoomWithSeats(1);
    Movie movie = createMovie();
    Projection projection = createProjection(movie, room);
    UUID seatId = room.getSeats().get(0).getId();

    CreateReservationRequest request = new CreateReservationRequest(projection.getId(), Set.of(seatId));
    return restTemplate
        .postForEntity(
            "/api/v1/reservations", new HttpEntity<>(request, authHeaders(ownerToken)), ReservationModel.class)
        .getBody();
  }
}
