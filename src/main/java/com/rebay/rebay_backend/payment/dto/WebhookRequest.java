package com.rebay.rebay_backend.payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class WebhookRequest { // 정적 중첩 클래스
    // JSON 구조 형태

    // 이벤트 타입 (ex. PAYMENT.CONFIRMED, PAYMENT.CANCELED..)
    @JsonProperty("eventType")
    private String eventType;

    // 이벤트 수신 시간
    @JsonProperty("createdAt")
    private LocalDateTime createAt;

    // 결제 데이터
    @JsonProperty("data")
    private PaymentData data;

    @Data
    public static class PaymentData{
        // 결제 키
        @JsonProperty("paymentKey")
        private String paymentKey;

        // 주문 ID
        @JsonProperty("orderId")
        private String orderId;

        // 결제 상태
        @JsonProperty("status")
        private String status;

        // 결제 금액
        @JsonProperty("totalAmount")
        private Integer totalAmount;

        // 결제 방법
        @JsonProperty("method")
        private String method;

        // 요청 시각
        @JsonProperty("requestedAt")
        private LocalDateTime requestedAt;

        // 승인 시각
        @JsonProperty("approvedAt")
        private LocalDateTime approvedAt;

//        // 취소 시각
//        @JsonProperty("canceledAt")
//        private LocalDateTime canceledAt;
//
//        // 취소 정보
//        @JsonProperty("cancels")
//        private Object cancels;


    }

}
