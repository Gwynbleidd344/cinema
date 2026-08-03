package com.example.demo.endpoint.rest.controller.movie;

import com.example.demo.endpoint.rest.controller.movie.dto.CreateMovieRequest;
import com.example.demo.endpoint.rest.controller.movie.dto.UpsertMovieRequest;
import com.example.demo.model.MovieModel;
import com.example.demo.model.mapper.MovieMapper;
import com.example.demo.service.MovieService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class MovieController {

  private final MovieService movieService;
  private final MovieMapper movieMapper;

  @GetMapping("/movies")
  public List<MovieModel> list() {
    return movieService.list().stream().map(movieMapper).toList();
  }

  @GetMapping("/movies/{id}")
  public MovieModel get(@PathVariable UUID id) {
    return movieMapper.apply(movieService.get(id));
  }

  @PostMapping("/movies")
  public MovieModel create(@RequestBody CreateMovieRequest request) {
    var saved =
        movieService.create(
            request.title(), request.genres(), request.description(), request.duration());
    return movieMapper.apply(saved);
  }

  @PutMapping("/movies")
  public MovieModel upsert(@RequestBody UpsertMovieRequest request) {
    var saved =
        movieService.upsert(
            request.id(),
            request.title(),
            request.genres(),
            request.description(),
            request.duration());
    return movieMapper.apply(saved);
  }
}
