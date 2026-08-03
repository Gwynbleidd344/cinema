package com.example.demo.repository;

import com.example.demo.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  // Used for authentication (login) and for enforcing unique emails on registration.
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
