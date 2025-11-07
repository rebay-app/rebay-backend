package com.rebay.rebay_backend.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long PostId;

    @NotNull(message = "구매자 ID는 필수입니다.")
    private Long buyerId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "금액은 0원 이상이어야 합니다.")
    private Integer amount;

}
