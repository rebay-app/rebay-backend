package com.rebay.rebay_backend.statistics.dto;

import com.rebay.rebay_backend.Post.dto.PostResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedPostDto {
    private PostResponse post;
    private double score;
}
