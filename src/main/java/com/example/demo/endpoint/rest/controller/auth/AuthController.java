package com.example.demo.endpoint.rest.controller.auth;

import com.example.demo.endpoint.rest.controller.auth.dto.AuthResponse;
import com.example.demo.endpoint.rest.controller.auth.dto.LoginRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
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
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    var user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
    return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole()));
  }
}
