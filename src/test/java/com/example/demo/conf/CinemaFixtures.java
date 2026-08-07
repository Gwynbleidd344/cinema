package com.example.demo.conf;

import com.example.demo.entity.Movie;
import com.example.demo.entity.Projection;
import com.example.demo.entity.Room;
import com.example.demo.entity.Seat;
import com.example.demo.entity.enums.Genre;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SeatRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CinemaFixtures {

  @Autowired private MovieRepository movieRepository;
  @Autowired private RoomRepository roomRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private ProjectionRepository projectionRepository;

  public record Fixture(Movie movie, Room room, Seat seatA, Seat seatB, Projection projection) {}

  public Fixture create() {
    Movie movie =
        movieRepository.save(
            Movie.builder()
                .title("Test Movie")
                .genres(Set.of(Genre.ACTION))
                .description("A movie used for integration tests")
                .duration(Duration.ofHours(2))
                .build());

    Room room = roomRepository.save(Room.builder().number("R1").capacity(50).build());

    Seat seatA = seatRepository.save(Seat.builder().number("A1").room(room).build());
    Seat seatB = seatRepository.save(Seat.builder().number("A2").room(room).build());

    Projection projection =
        projectionRepository.save(
            Projection.builder()
                .dateTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .seatPrice(new BigDecimal("12.50"))
                .movie(movie)
                .room(room)
                .build());

    return new Fixture(movie, room, seatA, seatB, projection);
  }

  /** A seat that belongs to a different room than the fixture's projection room. */
  public Seat foreignSeat() {
    Room otherRoom = roomRepository.save(Room.builder().number("R2").capacity(10).build());
    return seatRepository.save(Seat.builder().number("B1").room(otherRoom).build());
  }
}
