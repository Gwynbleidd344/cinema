package com.example.demo.endpoint;

import com.example.demo.endpoint.rest.controller.dto.AuthResponse;
import com.example.demo.endpoint.rest.controller.dto.LoginRequest;
import com.example.demo.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        var result = authService.login(request.email(), request.password());
        var user = result.user();
        return ResponseEntity.ok(new AuthResponse(result.token(), user.getId(), user.getRole()));
    }
}