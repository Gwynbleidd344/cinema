package com.example.demo.endpoint.rest.controller.dto;

import com.example.demo.entity.enums.Genre;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

public record UpsertMovieRequest(
    UUID id, String title, Set<Genre> genres, String description, Duration duration) {}
