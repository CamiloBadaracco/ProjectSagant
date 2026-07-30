package org.example.projectsagant.controller;

import jakarta.validation.Valid;
import org.example.projectsagant.dto.LoginRequest;
import org.example.projectsagant.dto.TokenResponse;
import org.example.projectsagant.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final String expectedUsername;
    private final String expectedPassword;

    public AuthController(JwtService jwtService,
                          @Value("${app.auth.username}") String expectedUsername,
                          @Value("${app.auth.password}") String expectedPassword) {
        this.jwtService = jwtService;
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!expectedUsername.equals(request.username()) || !expectedPassword.equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new TokenResponse(jwtService.generateToken(request.username())));
    }
}