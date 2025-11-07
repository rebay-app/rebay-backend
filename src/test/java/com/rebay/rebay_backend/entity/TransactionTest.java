package com.rebay.rebay_backend.entity;

import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionTest {

    @Test
    @DisplayName("Transaction 결제 승인 상태 변경 테스트")
    void transaction_결제승인_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();

        // When
        transaction.confirmPayment();

        // Then
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PAID);
    }

//    @Test
//    @DisplayName("Transaction 상품 수령 확인 테스트")
//    void transaction_상품수령확인_테스트() {
//        // Given
//        Transaction transaction = Transaction.builder()
//                .status(TransactionStatus.PAID)
//                .isReceived(false)
//                .build();
//
//        // When
//        transaction.confirmReceipt();
//
//        // Then
//        assertThat(transaction.getIsReceived()).isTrue();
//        assertThat(transaction.getReceivedAt()).isNotNull();
//        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SETTLEMENT_PENDING);
//    }

//    @Test
//    @DisplayName("Transaction 정산 완료 테스트")
//    void transaction_정산완료_테스트() {
//        // Given
//        Transaction transaction = Transaction.builder()
//                .status(TransactionStatus.SETTLEMENT_PENDING)
//                .build();
//
//        // When
//        transaction.completeSettlement();
//
//        // Then
//        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
//    }

}
