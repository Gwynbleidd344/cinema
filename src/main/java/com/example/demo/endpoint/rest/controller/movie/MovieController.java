package com.example.demo.endpoint.rest.controller.movie;

import com.example.demo.model.MovieModel;
import com.example.demo.model.mapper.MovieMapper;
import com.example.demo.service.MovieService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
