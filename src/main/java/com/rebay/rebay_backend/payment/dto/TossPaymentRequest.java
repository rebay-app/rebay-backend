package com.rebay.rebay_backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TossPaymentRequest {

    @NotBlank(message = "결제 Key는 필수입니다.")
    private String paymentKey; // 토스 결제 키

    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;    // 주문 ID

    @NotNull(message = "결제 금액은 필수입니다.")
    private Integer amount;
}
