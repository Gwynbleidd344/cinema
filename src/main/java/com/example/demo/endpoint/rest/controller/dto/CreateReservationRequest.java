package com.example.demo.endpoint.rest.controller.dto;

import java.util.Set;
import java.util.UUID;

public record CreateReservationRequest(UUID projectionId, Set<UUID> seatIds) {}