package com.example.demo.service;

import com.example.demo.entity.Movie;
import com.example.demo.entity.enums.Genre;
import com.example.demo.repository.MovieRepository;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class MovieService {

  private final MovieRepository movieRepository;

  public List<Movie> list() {
    return movieRepository.findAll();
  }

  public Movie get(UUID id) {
    return movieRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public Movie create(String title, Set<Genre> genres, String description, Duration duration) {
    Movie movie =
        Movie.builder()
            .title(title)
            .genres(genres)
            .description(description)
            .duration(duration)
            .build();
    return movieRepository.save(movie);
  }

  public Movie upsert(
      UUID id, String title, Set<Genre> genres, String description, Duration duration) {
    Movie movie = id == null ? new Movie() : get(id);
    movie.setTitle(title);
    movie.setGenres(genres);
    movie.setDescription(description);
    movie.setDuration(duration);
    return movieRepository.save(movie);
  }
}
