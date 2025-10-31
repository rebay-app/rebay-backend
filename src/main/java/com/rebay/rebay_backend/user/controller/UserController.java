package com.rebay.rebay_backend.user.controller;

import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(authenticationService.getCurrentUser());
    }
}
