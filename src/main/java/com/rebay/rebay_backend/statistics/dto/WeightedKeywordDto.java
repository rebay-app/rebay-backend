package com.rebay.rebay_backend.statistics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeightedKeywordDto {
    private final String keyword;
    private final double weight;
}
