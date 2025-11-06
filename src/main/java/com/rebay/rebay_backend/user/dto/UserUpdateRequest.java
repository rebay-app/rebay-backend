package com.rebay.rebay_backend.user.dto;

import com.rebay.rebay_backend.user.entity.AuthProvider;
import com.rebay.rebay_backend.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserUpdateRequest {
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private LocalDateTime updatedAt;
    private boolean isEnabled;

    public static UserUpdateRequest fromEntity(User user) {
        return UserUpdateRequest.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .updatedAt(user.getUpdatedAt())
                .isEnabled(user.isEnabled())
                .build();
    }
}
