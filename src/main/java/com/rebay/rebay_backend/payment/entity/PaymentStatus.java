package com.rebay.rebay_backend.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 준비"),
    DONE("결제 완료"),
    SETTLED("정산 완료"),
    CANCELED("결제 취소");

    private final String description;
}
