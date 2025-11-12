package com.rebay.rebay_backend.payment.controller;

import com.rebay.rebay_backend.payment.dto.TransactionResponse;
import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.payment.repository.TransactionRepository;
import com.rebay.rebay_backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final PaymentService paymentService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        TransactionResponse response = paymentService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/confirm-receipt")
    public ResponseEntity<TransactionResponse> confirmReceipt(
            @PathVariable Long transactionId,
            @RequestParam Long buyerId
    ) {
        try {
            TransactionResponse response = paymentService.confirmReceipt(transactionId, buyerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("거래 완료 확인 실패", e);
            throw new RuntimeException("거래 완료 확인에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<TransactionResponse>> getBuyerTransactions(@PathVariable Long buyerId) {
        List<TransactionResponse> responses = paymentService.getTransactionsByBuyerId(buyerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionResponse>> getSellerTransactions(@PathVariable Long sellerId) {
        List<TransactionResponse> responses = paymentService.getTransactionsBySellerId(sellerId);
        return ResponseEntity.ok(responses);
    }

}
