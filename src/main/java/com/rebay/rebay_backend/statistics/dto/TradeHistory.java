package com.rebay.rebay_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeHistory {
    private Long transactionId;
    private LocalDateTime purchasedAt;
    private BigDecimal price;
}
