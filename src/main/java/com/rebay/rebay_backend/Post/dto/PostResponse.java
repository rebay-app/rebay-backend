package com.rebay.rebay_backend.Post.dto;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.ProductCategory;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private BigDecimal price;
    private String imageUrl;
    private ProductCategory category;
    private SaleStatus status;
    private Integer viewCount;
    private UserDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private  boolean isLiked;
    private Long likeCount;


    public static PostResponse from(Post post) {

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .price(post.getPrice())
                .imageUrl(post.getImageUrl())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .status(post.getStatus())
                .user(UserDto.fromEntity(post.getUser()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
