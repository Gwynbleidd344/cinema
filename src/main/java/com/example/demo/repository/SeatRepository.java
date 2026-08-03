package com.example.demo.repository;

import com.example.demo.entity.Seat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

  List<Seat> findByRoom_Id(UUID roomId);

  List<Seat> findByIdIn(List<UUID> ids);
}
