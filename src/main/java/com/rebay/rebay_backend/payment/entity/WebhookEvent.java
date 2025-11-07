package com.rebay.rebay_backend.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)  // length: 데이터 무결성 보장, 효율성
    private String eventId; // 같은 이벤트가 중복 처리되지 않게 하기 위해 (멱등성 보장)

    @Column(nullable = false, length = 50)
    private String eventType;   // 어떤 이벤트인지 구분 (PAYMENT_PENDING, COMPLETED 등)

    @Column(nullable = false, length = 50)
    private String orderId;

    @Column(columnDefinition = "TEXT")
    private String payload; // 토스가 Webhook으로 보낸 원본 JSON 데이터
}
