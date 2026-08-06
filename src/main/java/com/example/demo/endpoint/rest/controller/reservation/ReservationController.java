package com.example.demo.endpoint.rest.controller.reservation;

import com.example.demo.endpoint.rest.controller.reservation.dto.CreateReservationRequest;
import com.example.demo.endpoint.rest.controller.reservation.dto.UpdateReservationRequest;
import com.example.demo.model.ReservationModel;
import com.example.demo.model.mapper.ReservationMapper;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.service.ReservationService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;
  private final ReservationMapper reservationMapper;

  /**
   * GET /reservations — 403 for CLIENT, 200 for MANAGER and EMPLOYEE.
   *
   * <p>Role check is enforced upstream by SecurityConfig (hasAnyRole MANAGER, EMPLOYEE), so this
   * method only runs for callers who are already allowed to see everything.
   */
  @GetMapping("/reservations")
  public List<ReservationModel> list() {
    return reservationService.list().stream().map(reservationMapper).toList();
  }

  /** POST /reservations — 200 for CLIENT (books for themselves), EMPLOYEE and MANAGER. */
  @PostMapping("/reservations")
  public ReservationModel create(@RequestBody CreateReservationRequest request) {
    UUID requesterId = AuthenticatedUser.id();
    var saved = reservationService.create(request.projectionId(), request.seatIds(), requesterId);
    return reservationMapper.apply(saved);
  }

  /**
   * GET /reservations/{id} — 200 if the CLIENT owns the reservation, 403 if it belongs to another
   * CLIENT, 200 for MANAGER and EMPLOYEE.
   */
  @GetMapping("/reservations/{id}")
  public ReservationModel get(@PathVariable UUID id) {
    UUID requesterId = AuthenticatedUser.id();
    boolean isPrivileged = AuthenticatedUser.hasAnyRole("MANAGER", "EMPLOYEE");

    var reservation = reservationService.get(id);
    boolean isOwner = reservation.getCreatedBy().getId().equals(requesterId);
    if (!isPrivileged && !isOwner) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    return reservationMapper.apply(reservation);
  }

  /**
   * PUT /reservation — 200 for CLIENTS updating their own reservation (but only EMPLOYEES /
   * MANAGERS may validate it, i.e. set status to SUCCESS), 200 for EMPLOYEES and MANAGERS.
   */
  @PutMapping("/reservation")
  public ReservationModel update(@RequestBody UpdateReservationRequest request) {
    UUID requesterId = AuthenticatedUser.id();
    boolean isEmployeeOrManager = AuthenticatedUser.hasAnyRole("MANAGER", "EMPLOYEE");

    var updated =
        reservationService.update(
            request.id(), request.status(), request.seatIds(), requesterId, isEmployeeOrManager);
    return reservationMapper.apply(updated);
  }
}
