package com.rebay.rebay_backend.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.payment.dto.WebhookRequest;
import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.PaymentStatus;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.WebhookEvent;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.payment.repository.TransactionRepository;
import com.rebay.rebay_backend.payment.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookEventRepository webhookEventRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private PostRepository postRepository;
    private final ObjectMapper objectMapper;

    // 웹훅 이벤트 처리
    public void processWebhookEvent(String payload, WebhookRequest webhookRequest) {
        String eventId = generateEventId(webhookRequest);

        // 멱등성 체크: 이미 처리된 이벤트인지 확인
        if (webhookEventRepository.existsByEventId(eventId)) {
            log.info("이미 처리된 웹훅 이벤트: eventId={}", eventId);
            return;
        }

        // 웹훅 이벤트 저장
        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventId(eventId)
                .eventType(webhookRequest.getEventType())
                .paymentKey(webhookRequest.getData().getPaymentKey())
                .orderId(webhookRequest.getData().getOrderId())
                .payload(payload)
                .processed(false)
                .build();

        webhookEventRepository.save(webhookEvent);

        try {
            // 이벤트 타입 별 처리
            processEventByType(webhookRequest);

            // 처리 성공 표시
            webhookEvent.markAsProcessed();
            webhookEventRepository.save(webhookEvent);

            log.info("웹훅 이벤트 처리 완료: eventId={}, eventType={}", eventId, webhookRequest.getEventType());

        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 실패: eventId={}, error={}", eventId, e.getMessage(), e);

            // 처리 실패 기록
            webhookEvent.markAsFailed(e.getMessage());
            webhookEventRepository.save(webhookEvent);

            throw new RuntimeException("웹훅 이벤트 처리 실패", e);
        }
    }

    // 이벤트 타입에 따른 처리 분기
    private void processEventByType(WebhookRequest webhookRequest) {
        String eventType = webhookRequest.getEventType();
        String orderId = webhookRequest.getData().getOrderId();

        switch (eventType) {
            case "PAYMENT.CONFIRMED":
                handlePaymentConfirmed(webhookRequest);
                break;

//            case "PAYMENT.CANCELED":
//                handlePaymentCanceled(webhookRequest);
//                break;
//
//            case "PAYMENT.PARTIAL_CANCELED":
//                handlePaymentPartialCanceled(webhookRequest);
//                break;
//
//            case "VIRTUAL_ACCOUNT_ISSUED":
//                handleVirtualAccountIssued(webhookRequest);
//                break;

            default:
                log.warn("처리되지 않은 이벤트 타입: eventType={}, orderId={}", eventType, orderId);
        }
    }

    // 결제 승인 완료 이벤트 처리
    // 클라이언트에서 승인 못한 경우 백업으로 동작
    private void handlePaymentConfirmed(WebhookRequest webhookRequest) {
        String orderId = webhookRequest.getData().getOrderId();
        String paymentKey = webhookRequest.getData().getPaymentKey();

        log.info("결제 승인 웹훅 수신: orderId={}, paymentKey={}", orderId, paymentKey);

        // 안전결제 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        // 이미 승인된 경우 건너뜀
        if (payment.getPaymentStatus() == PaymentStatus.DONE) {
            log.info("이미 승인된 결제: orderId={}", orderId);
            return;
        }

        // 결제 승인 처리
        payment.approve(
                paymentKey,
                webhookRequest.getData().getMethod(),
                null    // Receipt URL은 웹훅에 포함되지 않을 수 있음
        );

        // 거래 상태 업데이트
        Transaction transaction = payment.getTransaction();
        transaction.confirmPayment();

        // 상품 상태 업데이트
        Post post = transaction.getPost();
        post.setStatus(SaleStatus.SOLD);

        paymentRepository.save(payment);
        transactionRepository.save(transaction);
        postRepository.save(post);

        log.info("웹훅을 통한 결제 승인 완료: orderId={}, paymentKey={}", orderId, paymentKey);
    }

    // 고유한 이벤트 ID 생성
    private String generateEventId(WebhookRequest webhookRequest) {
        // 토스에서 제공하는 고유 ID가 없는 경우
        // eventType + orderId + paymentKey 조합으로 EventId 생성

        String eventType = webhookRequest.getEventType();
        String orderId = webhookRequest.getData().getOrderId();
        String paymentKey = webhookRequest.getData().getPaymentKey();

        return String.format("%s_%s_%s", eventType, orderId, paymentKey != null ? paymentKey : UUID.randomUUID().toString());
    }

}
