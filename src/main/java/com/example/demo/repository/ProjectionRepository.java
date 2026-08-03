package com.example.demo.repository;

import com.example.demo.entity.Projection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectionRepository extends JpaRepository<Projection, UUID> {

  List<Projection> findByMovie_Id(UUID movieId);

  List<Projection> findByRoom_Id(UUID roomId);

  List<Projection> findByDateTimeBetween(Instant from, Instant to);
}
