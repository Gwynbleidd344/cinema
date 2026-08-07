package com.example.demo.endpoint.rest.controller.room;

import com.example.demo.endpoint.rest.controller.room.dto.UpsertRoomRequest;
import com.example.demo.model.RoomModel;
import com.example.demo.model.mapper.RoomMapper;
import com.example.demo.service.RoomService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class RoomController {

  private final RoomService roomService;
  private final RoomMapper roomMapper;

  @GetMapping("/rooms")
  public List<RoomModel> list() {
    return roomService.list().stream().map(roomMapper).toList();
  }

  @GetMapping("/rooms/{id}")
  public RoomModel get(@PathVariable UUID id) {
    return roomMapper.apply(roomService.get(id));
  }

  @PutMapping("/rooms")
  public RoomModel upsert(@RequestBody UpsertRoomRequest request) {
    var saved = roomService.upsert(request.id(), request.number(), request.capacity());
    return roomMapper.apply(saved);
  }
}
