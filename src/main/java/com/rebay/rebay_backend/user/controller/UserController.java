package com.rebay.rebay_backend.user.controller;

import com.rebay.rebay_backend.user.dto.PasswordUpdateRequest;
import com.rebay.rebay_backend.user.dto.UserDto;
import com.rebay.rebay_backend.user.dto.UserResponse;
import com.rebay.rebay_backend.user.dto.UserUpdateRequest;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.service.AuthenticationService;
import com.rebay.rebay_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(UserDto.fromEntity(authenticationService.getCurrentUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Boolean> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        return ResponseEntity.ok(userService.updatePassword(request));
    }
}
