package com.rebay.rebay_backend.payment.repository;

import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import com.rebay.rebay_backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository <Payment, Long>{
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByTransactionId(Long transactionId);

}
