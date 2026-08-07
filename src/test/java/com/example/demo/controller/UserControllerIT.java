package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.endpoint.rest.controller.dto.RegisterUserRequest;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.model.UserModel;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void register_creates_a_client_user() {
    String email = "new-" + UUID.randomUUID() + "@test.local";

    ResponseEntity<UserModel> response =
        restTemplate.postForEntity(
            "/api/v1/users",
            new RegisterUserRequest(
                "Jane", "Doe", LocalDate.of(1995, 5, 20), email, "0341234567", "s3cret!"),
            UserModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(email);
    assertThat(response.getBody().role()).isEqualTo(UserRole.CLIENT);
    assertThat(response.getBody().id()).isNotNull();
  }

  @Test
  void register_rejects_duplicate_email() {
    String email = "dup-" + UUID.randomUUID() + "@test.local";
    RegisterUserRequest request =
        new RegisterUserRequest(
            "Jane", "Doe", LocalDate.of(1995, 5, 20), email, "0341234567", "s3cret!");

    restTemplate.postForEntity("/api/v1/users", request, UserModel.class);

    ResponseEntity<String> secondAttempt =
        restTemplate.postForEntity("/api/v1/users", request, String.class);

    assertThat(secondAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}
