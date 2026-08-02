package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.Room;
import com.example.demo.entity.Seat;
import com.example.demo.model.RoomModel;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class RoomMapper implements Function<Room, RoomModel> {

  @Override
  public RoomModel apply(Room room) {
    return new RoomModel(
        room.getId(),
        room.getNumber(),
        room.getCapacity(),
        room.getSeats().stream().map(Seat::getId).toList());
  }
}
