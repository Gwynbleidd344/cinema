package com.example.demo.endpoint.rest.controller.dto;

import com.example.demo.entity.enums.ReservationStatus;
import java.util.Set;
import java.util.UUID;

public record UpdateReservationRequest(UUID id, ReservationStatus status, Set<UUID> seatIds) {}