package com.example.demo.service;

import com.example.demo.entity.Room;
import com.example.demo.repository.RoomRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;

  @Transactional(readOnly = true)
  public List<Room> list() {
    return roomRepository.findAll();
  }

  @Transactional
  public Room get(UUID id) {
    return roomRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @Transactional
  public Room upsert(UUID id, String number, Integer capacity) {
    Room room = id == null ? new Room() : get(id);
    room.setNumber(number);
    room.setCapacity(capacity);
    return roomRepository.save(room);
  }
}
