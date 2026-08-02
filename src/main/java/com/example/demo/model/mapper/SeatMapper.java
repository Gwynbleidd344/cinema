package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.Seat;
import com.example.demo.model.SeatModel;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class SeatMapper implements Function<Seat, SeatModel> {

  @Override
  public SeatModel apply(Seat seat) {
    return new SeatModel(seat.getId(), seat.getNumber(), seat.getRoom().getId());
  }
}
