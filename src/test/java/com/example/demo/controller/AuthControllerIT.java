package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.conf.TestUserFactory;
import com.example.demo.endpoint.rest.controller.dto.AuthResponse;
import com.example.demo.endpoint.rest.controller.dto.LoginRequest;
import com.example.demo.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TestUserFactory testUserFactory;

  @Test
  void login_succeeds_with_correct_credentials() {
    var testUser = testUserFactory.create(UserRole.CLIENT, "correct-password");

    ResponseEntity<AuthResponse> response =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginRequest(testUser.user().getEmail(), "correct-password"),
            AuthResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().userId()).isEqualTo(testUser.user().getId());
    assertThat(response.getBody().role()).isEqualTo(UserRole.CLIENT);
    assertThat(response.getBody().token()).isNotBlank();
  }

  @Test
  void login_fails_with_wrong_password() {
    var testUser = testUserFactory.create(UserRole.CLIENT, "correct-password");

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginRequest(testUser.user().getEmail(), "wrong-password"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void login_fails_for_unknown_email() {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginRequest("nobody-" + System.nanoTime() + "@test.local", "whatever"),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
