package com.example.demo.model;

import java.util.List;
import java.util.UUID;

public record RoomModel(UUID id, String number, Integer capacity, List<UUID> seatIds) {}
