package com.rebay.rebay_backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {

    @JsonProperty("paymentKey")
    private String paymentKey;

    @JsonProperty("method")
    private String method;

    @JsonProperty("receipt")
    private Receipt receipt;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("totalAmount")
    private Integer totalAmount;

    @JsonProperty("approvedAt")
    private String approvedAt;

    @JsonProperty("requestedAt")
    private String requestedAt;

    // 에러 처리용
    @JsonProperty("failure")
    private Failure failure;

    @Data
    public static class Receipt {
        @JsonProperty("url")
        private String url;
    }

    @Data
    public static class Failure {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;
    }
}
