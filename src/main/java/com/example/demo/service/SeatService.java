package com.example.demo.service;

import com.example.demo.entity.Seat;
import com.example.demo.repository.SeatRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class SeatService {

  private final SeatRepository seatRepository;

  public List<Seat> list(UUID roomId) {
    return roomId == null ? seatRepository.findAll() : seatRepository.findByRoom_Id(roomId);
  }

  public Seat get(UUID id) {
    return seatRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
