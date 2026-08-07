package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.conf.TestUserFactory;
import com.example.demo.endpoint.rest.controller.dto.UpsertRoomRequest;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.RoomModel;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RoomControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TestUserFactory testUserFactory;

  @Test
  void manager_can_create_and_list_rooms() {
    var manager = testUserFactory.create(UserRole.MANAGER);
    RoomModel created = upsertRoom(manager.token(), null, "101", 30).getBody();

    ResponseEntity<RoomModel[]> listResponse =
        restTemplate.exchange(
            "/api/v1/rooms",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(manager.token())),
            RoomModel[].class);

    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).extracting(RoomModel::id).contains(created.id());
  }

  @Test
  void employee_can_list_rooms() {
    var employee = testUserFactory.create(UserRole.EMPLOYEE);

    ResponseEntity<RoomModel[]> response =
        restTemplate.exchange(
            "/api/v1/rooms",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(employee.token())),
            RoomModel[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void client_cannot_list_rooms() {
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/rooms",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(client.token())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void client_cannot_upsert_room() {
    var client = testUserFactory.create(UserRole.CLIENT);

    ResponseEntity<String> response = upsertRoom(client.token(), null, "999", 5, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_upsert_room() {
    var employee = testUserFactory.create(UserRole.EMPLOYEE);

    ResponseEntity<String> response = upsertRoom(employee.token(), null, "999", 5, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_update_existing_room() {
    var manager = testUserFactory.create(UserRole.MANAGER);
    RoomModel created = upsertRoom(manager.token(), null, "201", 40).getBody();

    ResponseEntity<RoomModel> updated = upsertRoom(manager.token(), created.id(), "201-bis", 60);

    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody().id()).isEqualTo(created.id());
    assertThat(updated.getBody().number()).isEqualTo("201-bis");
    assertThat(updated.getBody().capacity()).isEqualTo(60);
  }

  @Test
  void getting_unknown_room_returns_404() {
    var manager = testUserFactory.create(UserRole.MANAGER);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/rooms/" + UUID.randomUUID(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(manager.token())),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private ResponseEntity<RoomModel> upsertRoom(String token, UUID id, String number, int capacity) {
    return upsertRoom(token, id, number, capacity, RoomModel.class);
  }

  private <T> ResponseEntity<T> upsertRoom(
      String token, UUID id, String number, int capacity, Class<T> responseType) {
    HttpEntity<UpsertRoomRequest> entity =
        new HttpEntity<>(new UpsertRoomRequest(id, number, capacity), authHeaders(token));
    return restTemplate.exchange("/api/v1/rooms", HttpMethod.PUT, entity, responseType);
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return headers;
  }
}
