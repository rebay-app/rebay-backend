package com.rebay.rebay_backend.review.dto;

import com.rebay.rebay_backend.review.entity.StarRating;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequest {
    private String content;
    private int rating;
}
