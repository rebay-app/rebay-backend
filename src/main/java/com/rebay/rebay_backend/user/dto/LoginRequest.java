package com.rebay.rebay_backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    private String username;
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
