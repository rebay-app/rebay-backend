package com.rebay.rebay_backend.entity;

import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.PaymentStatus;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {

    @Test
    @DisplayName("EscrowPayment 생성 테스트")
    void payment_생성_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();

        String orderId = "ORDER_TEST123";
        BigDecimal amount = BigDecimal.valueOf(10000);

        // When
        Payment payment = Payment.create(transaction, orderId, amount);

        // Then
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getTransaction()).isEqualTo(transaction);
    }

    @Test
    @DisplayName("결제 승인 상태 변경 테스트")
    void payment_승인_상태변경_테스트() {
        // Given
        Payment payment = Payment.builder()
                .orderId("ORDER_TEST123")
                .amount(BigDecimal.valueOf(10000))
                .paymentStatus(PaymentStatus.READY)
                .build();

        String paymentKey = "test_payment_key_123";
        String method = "카드";
        String receiptUrl = "https://example.com/receipt";

        // When
        payment.approve(paymentKey, method, receiptUrl);

        // Then
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(payment.getMethod()).isEqualTo(method);
        assertThat(payment.getReceiptUrl()).isEqualTo(receiptUrl);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getApprovedAt()).isNotNull();
    }
}
