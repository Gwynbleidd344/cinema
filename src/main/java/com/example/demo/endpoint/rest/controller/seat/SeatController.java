package com.example.demo.endpoint.rest.controller.seat;

import com.example.demo.endpoint.rest.controller.seat.dto.UpsertSeatRequest;
import com.example.demo.model.SeatModel;
import com.example.demo.model.mapper.SeatMapper;
import com.example.demo.service.SeatService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class SeatController {

  private final SeatService seatService;
  private final SeatMapper seatMapper;

  @GetMapping("/seats")
  public List<SeatModel> list(@RequestParam(required = false) UUID roomId) {
    return seatService.list(roomId).stream().map(seatMapper).toList();
  }

  @GetMapping("/seats/{id}")
  public SeatModel get(@PathVariable UUID id) {
    return seatMapper.apply(seatService.get(id));
  }

  @PutMapping("/seats")
  public SeatModel upsert(@RequestBody UpsertSeatRequest request) {
    var saved = seatService.upsert(request.id(), request.number(), request.roomId());
    return seatMapper.apply(saved);
  }
}
