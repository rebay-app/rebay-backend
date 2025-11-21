package com.rebay.rebay_backend.user.dto;

import com.rebay.rebay_backend.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private boolean isEnabled;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .isEnabled(user.isEnabled())
                .build();
    }
}
