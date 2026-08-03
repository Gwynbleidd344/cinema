package com.example.demo.repository;

import com.example.demo.entity.Movie;
import com.example.demo.entity.enums.Genre;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

  List<Movie> findByGenresContaining(Genre genre);

  List<Movie> findByTitleContainingIgnoreCase(String title);
}
