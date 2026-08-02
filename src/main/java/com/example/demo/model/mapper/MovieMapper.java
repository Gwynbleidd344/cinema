package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.Movie;
import com.example.demo.model.MovieModel;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class MovieMapper implements Function<Movie, MovieModel> {

  @Override
  public MovieModel apply(Movie movie) {
    return new MovieModel(
        movie.getId(),
        movie.getTitle(),
        movie.getGenres(),
        movie.getDescription(),
        movie.getDuration());
  }
}
