package com.example.demo.model;

import com.example.demo.entity.enums.UserRole;
import java.time.LocalDate;
import java.util.UUID;

public record UserModel(
    UUID id,
    String firstName,
    String lastName,
    LocalDate birthdate,
    String email,
    String phone,
    UserRole role) {}
