package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectionModel(
    UUID id, Instant dateTime, BigDecimal seatPrice, UUID movieId, UUID roomId) {}
