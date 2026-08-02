package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Seat;
import com.example.demo.entity.User;
import com.example.demo.model.ReservationModel;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class ReservationMapper implements Function<Reservation, ReservationModel> {

  @Override
  public ReservationModel apply(Reservation reservation) {
    return new ReservationModel(
        reservation.getId(),
        reservation.getCreatedAt(),
        reservation.getStatus(),
        reservation.getProjection().getId(),
        reservation.getSeats().stream().map(Seat::getId).collect(Collectors.toSet()),
        reservation.getCreatedBy().getId(),
        Optional.ofNullable(reservation.getProcessedBy()).map(User::getId).orElse(null));
  }
}
