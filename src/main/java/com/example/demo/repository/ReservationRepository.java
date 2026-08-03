package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.enums.ReservationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

  Optional<Reservation> findByIdAndCreatedBy_Id(UUID id, UUID createdById);

  List<Reservation> findByCreatedBy_Id(UUID createdById);

  List<Reservation> findByProjection_Id(UUID projectionId);

  List<Reservation> findByStatus(ReservationStatus status);
}
