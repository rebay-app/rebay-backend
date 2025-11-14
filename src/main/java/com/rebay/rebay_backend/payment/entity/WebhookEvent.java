package com.rebay.rebay_backend.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events", indexes = {
        @Index(name = "idx_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_order_id", columnList = "orderId")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토스 페이먼츠에서 제공하는 이벤트 고유 ID
    // 같은 이벤트가 중복 처리되지 않게 하기 위해 (멱등성 보장)
    @Column(nullable = false, unique = true, length = 100)  // length: 데이터 무결성 보장, 효율성
    private String eventId;

    // 이벤트 타입 (PAYMENT_PENDING, COMPLETED 등)
    @Column(nullable = false, length = 50)
    private String eventType;

    // 토스페이먼츠 결제 키
    @Column(length = 200)
    private String paymentKey;

    // 주문 ID
    @Column(nullable = false, length = 100)
    private String orderId;

    // 웹훅 전체 payload (JSON)
    @Column(columnDefinition = "TEXT")
    private String payload;

    // 처리 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    // 처리 완료 시각
    private LocalDateTime processedAt;

    // 처리 실패 시 에러 메세지
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // 이벤트 수신 시간
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    // 웹훅 이벤트 처리 성공
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    // 웹훅 이벤트 처리 실패
    public void markAsFailed(String errorMessage) {
        this.processed = false;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
}
