package com.rebay.rebay_backend.user.controller;

import com.rebay.rebay_backend.user.dto.AuthResponse;
import com.rebay.rebay_backend.user.dto.LoginRequest;
import com.rebay.rebay_backend.user.dto.RegisterRequest;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
