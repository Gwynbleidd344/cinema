package com.example.demo.endpoint.rest.controller.dto;

import java.util.UUID;

public record UpsertSeatRequest(UUID id, String number, UUID roomId) {}
