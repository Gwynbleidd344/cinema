package com.example.demo.endpoint.rest.controller.dto;

import com.example.demo.entity.enums.UserRole;
import java.util.UUID;

public record AuthResponse(String token, UUID userId, UserRole role) {}
