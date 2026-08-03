package com.example.demo.endpoint.rest.controller.user.dto;

import java.time.LocalDate;

public record RegisterUserRequest(
    String firstName,
    String lastName,
    LocalDate birthdate,
    String email,
    String phone,
    String password) {}
