package com.example.demo.model;

import com.example.demo.entity.enums.ReservationStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ReservationModel(
    UUID id,
    Instant createdAt,
    ReservationStatus status,
    UUID projectionId,
    Set<UUID> seatIds,
    UUID createdById,
    UUID processedById) {}
