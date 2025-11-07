package com.rebay.rebay_backend.payment.entity;

import com.rebay.rebay_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="transaction_id", unique = true, nullable = false)
    private Transaction transaction;      // 주문 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String orderId;     // 토스에서 결제를 식별하기 위해 쓰는 ID

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String paymentKey;  // 토스 결제 키

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus transactionStatus = TransactionStatus.PAYMENT_PENDING;

    @Column
    private String method;  // 주문 방법

    @Column
    private LocalDateTime approvedAt;   // 결제 시간

    @Column
    private String receiptUrl;  // 영수증 주소

    public static Payment create(Transaction transaction, String orderId, BigDecimal amount) {
        return Payment.builder()
                .transaction(transaction)
                .user(transaction.getBuyer())
                .orderId(orderId)
                .amount(amount)
                .paymentStatus(PaymentStatus.READY)
                .build();
    }

    public void approve(String paymentKey, String method, String receiptUrl) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.paymentStatus = PaymentStatus.DONE;
        this.approvedAt = LocalDateTime.now();
        this.receiptUrl = receiptUrl;
    }
}
