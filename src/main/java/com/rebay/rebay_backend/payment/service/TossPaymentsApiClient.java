package com.rebay.rebay_backend.payment.service;

import com.rebay.rebay_backend.payment.config.TossPaymentConfig;
import com.rebay.rebay_backend.payment.dto.TossPaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;


@Service
@Slf4j
public class TossPaymentsApiClient {

    private final TossPaymentConfig config;
    private final WebClient webClient;

    public TossPaymentsApiClient(TossPaymentConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getApi().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossPaymentResponse confirmPayment(TossPaymentRequest request) {
        String auth = createAuthHeader();

        log.info("토스페이먼츠 결제 승인 요청 시작 - Payment Key: {}, Order ID: {}, Amount: {}",
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

        try {
            TossPaymentResponse response = webClient.post()
                    .uri("/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .bodyValue(Map.of(
                            "paymentKey", request.getPaymentKey(),
                            "orderId", request.getOrderId(),
                            "amount", request.getAmount()
                    ))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        log.error("토스 API 에러 응답 [{}]: {}",
                                                clientResponse.statusCode(), errorBody);
                                        return new RuntimeException(
                                                "토스 API 에러 [" + clientResponse.statusCode() + "]: " + errorBody);
                                    })
                    )
                    .bodyToMono(TossPaymentResponse.class)
                    .block();

            log.info("토스페이먼츠 결제 승인 성공: {}", response);
            return response;
        } catch (Exception e) {
            log.error("토스페이먼츠 API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("결제 승인 API 호출 실패: " + e.getMessage());
        }
    }

    private String createAuthHeader() {
        String credentials = config.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
