package com.example.demo.service;

import com.example.demo.entity.Projection;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Seat;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.ReservationStatus;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final ProjectionRepository projectionRepository;
  private final SeatRepository seatRepository;
  private final UserRepository userRepository;

  public List<Reservation> list() {
    return reservationRepository.findAll();
  }

  public Reservation get(UUID id) {
    return reservationRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public Reservation create(UUID projectionId, Set<UUID> seatIds, UUID requesterId) {
    if (seatIds == null || seatIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds must not be empty");
    }
    if (projectionId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectionId is required");
    }

    Projection projection =
        projectionRepository
            .findById(projectionId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projection not found"));

    Set<Seat> seats = resolveAndValidateSeats(seatIds, projection);
    ensureSeatsAreFree(projectionId, seatIds, null);

    User createdBy =
        userRepository
            .findById(requesterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    Reservation reservation =
        Reservation.builder()
            .createdAt(Instant.now())
            .status(ReservationStatus.PENDING)
            .projection(projection)
            .seats(seats)
            .createdBy(createdBy)
            .build();

    return reservationRepository.save(reservation);
  }

  public Reservation update(
      UUID id,
      ReservationStatus newStatus,
      Set<UUID> seatIds,
      UUID requesterId,
      boolean isEmployeeOrManager) {
    Reservation reservation = get(id);

    boolean isOwner = reservation.getCreatedBy().getId().equals(requesterId);
    if (!isEmployeeOrManager && !isOwner) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    boolean attemptsValidation =
        newStatus == ReservationStatus.SUCCESS && reservation.getStatus() != ReservationStatus.SUCCESS;
    if (attemptsValidation && !isEmployeeOrManager) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only an employee or manager may validate a reservation");
    }

    if (seatIds != null && !seatIds.isEmpty()) {
      Set<Seat> seats = resolveAndValidateSeats(seatIds, reservation.getProjection());
      ensureSeatsAreFree(reservation.getProjection().getId(), seatIds, reservation.getId());
      reservation.setSeats(seats);
    }

    if (newStatus != null) {
      reservation.setStatus(newStatus);
      if (attemptsValidation) {
        User processedBy =
            userRepository
                .findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        reservation.setProcessedBy(processedBy);
      }
    }

    return reservationRepository.save(reservation);
  }

  private Set<Seat> resolveAndValidateSeats(Set<UUID> seatIds, Projection projection) {
    List<Seat> seats = seatRepository.findByIdIn(new ArrayList<>(seatIds));
    if (seats.size() != seatIds.size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more seats not found");
    }

    UUID roomId = projection.getRoom().getId();
    boolean allSeatsInRoom = seats.stream().allMatch(seat -> seat.getRoom().getId().equals(roomId));
    if (!allSeatsInRoom) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Seats must belong to the projection's room");
    }

    return new HashSet<>(seats);
  }

  /** Rejects seats already held by a non-canceled reservation for the same projection. */
  private void ensureSeatsAreFree(UUID projectionId, Set<UUID> seatIds, UUID excludingReservationId) {
    Set<UUID> takenSeatIds =
        reservationRepository.findByProjection_Id(projectionId).stream()
            .filter(r -> r.getStatus() != ReservationStatus.CANCELED)
            .filter(r -> excludingReservationId == null || !r.getId().equals(excludingReservationId))
            .flatMap(r -> r.getSeats().stream())
            .map(Seat::getId)
            .collect(Collectors.toSet());

    boolean overlap = seatIds.stream().anyMatch(takenSeatIds::contains);
    if (overlap) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more seats already reserved");
    }
  }
}
