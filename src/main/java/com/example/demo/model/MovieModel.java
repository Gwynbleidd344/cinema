package com.example.demo.model;

import com.example.demo.entity.enums.Genre;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

public record MovieModel(
    UUID id, String title, Set<Genre> genres, String description, Duration duration) {}
