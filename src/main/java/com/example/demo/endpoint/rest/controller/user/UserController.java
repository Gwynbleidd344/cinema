package com.example.demo.endpoint.rest.controller.user;

import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.endpoint.rest.controller.user.dto.RegisterUserRequest;
import com.example.demo.model.UserModel;
import com.example.demo.model.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @PostMapping
  public ResponseEntity<UserModel> register(@RequestBody RegisterUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
    }

    User user =
        User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .birthdate(request.birthdate())
            .email(request.email())
            .phone(request.phone())
            .password(passwordEncoder.encode(request.password()))
            // Self sign-up always creates a CLIENT: never trust a role coming from the request
            // body, or anyone could register as MANAGER.
            .role(UserRole.CLIENT)
            .build();

    User saved = userRepository.save(user);
    return ResponseEntity.ok(userMapper.apply(saved));
  }
}
