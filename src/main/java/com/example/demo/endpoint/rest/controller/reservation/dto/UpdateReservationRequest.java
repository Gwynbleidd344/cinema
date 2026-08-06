package com.example.demo.endpoint.rest.controller.reservation.dto;

import com.example.demo.entity.enums.ReservationStatus;
import java.util.Set;
import java.util.UUID;

/**
 * Body for {@code PUT /api/v1/reservation}.
 *
 * <p>{@code seatIds} is optional: pass {@code null} or an empty set to leave the seat selection
 * unchanged. {@code status} is optional too: pass {@code null} to leave the status unchanged.
 * Setting {@code status} to {@code SUCCESS} (validating the reservation) is only allowed for
 * EMPLOYEE and MANAGER — a CLIENT attempting it gets a 403.
 */
public record UpdateReservationRequest(UUID id, ReservationStatus status, Set<UUID> seatIds) {}
