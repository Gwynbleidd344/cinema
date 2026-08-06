package com.example.demo.endpoint.rest.controller.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class HealthControllerIT extends AbstractIntegrationTest {

  @Test
  void ping_returns_pong() {
    ResponseEntity<String> response = restTemplate.getForEntity("/ping", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("pong");
  }

  @Test
  void health_db_returns_ok() {
    ResponseEntity<String> response = restTemplate.getForEntity("/health/db", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }
}