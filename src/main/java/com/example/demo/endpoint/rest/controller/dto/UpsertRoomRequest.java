package com.example.demo.endpoint.rest.controller.dto;

import java.util.UUID;

public record UpsertRoomRequest(UUID id, String number, Integer capacity) {}
