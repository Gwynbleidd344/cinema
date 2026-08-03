package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User register(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String email,
      String phone,
      String rawPassword) {
    if (userRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
    }

    User user =
        User.builder()
            .firstName(firstName)
            .lastName(lastName)
            .birthdate(birthdate)
            .email(email)
            .phone(phone)
            .password(passwordEncoder.encode(rawPassword))
            .role(UserRole.CLIENT)
            .build();

    return userRepository.save(user);
  }
}
