package com.rebay.rebay_backend.payment.controller;

import com.rebay.rebay_backend.payment.config.TossPaymentConfig;
import com.rebay.rebay_backend.payment.dto.PaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentRequest;
import com.rebay.rebay_backend.payment.dto.TransactionResponse;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentConfig tossPaymentConfig;

    @GetMapping("/client-key")
    public ResponseEntity<Map<String, String>> getClientKey(){
        return ResponseEntity.ok(Map.of("clientKey", tossPaymentConfig.getClientKey()));
    }

    @PostMapping("/prepare")
    public ResponseEntity<TransactionResponse> preparePayment(@Valid @RequestBody PaymentRequest request) {
        TransactionResponse response = paymentService.preparePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<TransactionResponse> confirmPayment(@Valid @RequestBody TossPaymentRequest request) {
        try {
            TransactionResponse response = paymentService.confirmPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            throw new RuntimeException("결제 승인에 실패했습니다: " + e.getMessage());
        }
    }
}
