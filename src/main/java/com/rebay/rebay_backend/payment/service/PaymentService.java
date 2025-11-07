package com.rebay.rebay_backend.payment.service;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.payment.dto.PaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentResponse;
import com.rebay.rebay_backend.payment.dto.TransactionResponse;
import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.payment.repository.TransactionRepository;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsApiClient tossPaymentsApiClient;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    // 결제 준비 : Transaction, Payment 생성
    public TransactionResponse preparePayment(PaymentRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다." + request.getPostId()));

        // 거래 게시글 상태 확인
        if (post.getStatus() != SaleStatus.ON_SALE) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다.");
        }

        // 구매자 조회
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다: " + request.getBuyerId()));

        // 판매 요청자가 판매자인지 확인
        User seller = post.getUser();
        if (seller.getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("자신의 상품은 구매할 수 없습니다.");
        }

        // 거래 준비
        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)  // 거래 완료(물품 수령) 여부
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        String orderId = generateOrderId();
        BigDecimal amount = post.getPrice();

        // 에스크로 결제

        // 결제 준비 완료
        log.info("준비 완료: orderId={}, amount={}", orderId, amount, post.getId());

        // DTO 생성
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .postId(post.getId())
                .productName(post.getTitle())
                .amount(post.getPrice())
                .buyerId(buyer.getId())
                .buyerName(buyer.getUsername())
                .sellerId(seller.getId())
                .sellerName(seller.getUsername())
                .isReceived(savedTransaction.getIsReceived())
                .receivedAt(savedTransaction.getReceivedAt())
                .status(savedTransaction.getStatus())
                .orderId(orderId)
                .createdAt(savedTransaction.getCreatedAt())
                .build();
    }

    // 결제 승인
    @Transactional
    public TransactionResponse confirmPayment(TossPaymentRequest request) {

        // 결제 조회
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(()->new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 금액 검증
        BigDecimal requestAmount = BigDecimal.valueOf(request.getAmount());
        if(payment.getAmount().compareTo(requestAmount) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        try{
            // 토스페이먼츠 결제 승인
            TossPaymentResponse tossPaymentResponse = tossPaymentsApiClient.confirmPayment(request);

            payment.approve(
                    tossPaymentResponse.getPaymentKey(),
                    tossPaymentResponse.getMethod(),
                    tossPaymentResponse.getReceipt() != null ? tossPaymentResponse.getReceipt().getUrl() : null
            );

            // 거래 상태 업데이트 - 결제 완료(에스크로 예치)
            Transaction transaction = payment.getTransaction();
            transaction.confirmPayment();

            // 게시글 상태 업데이트
            Post post = transaction.getPost();
            post.setStatus(SaleStatus.SOLD);

//            postRepository.save(post);
//            paymentRepository.save(payment);
//            transactionRepository.save(transaction);

            log.info("결제 승인 완료 (에스크로 예치): paymentKey={}, orderId={}", request.getPaymentKey(), request.getOrderId());

            // Lazy Loading 방지를 위해 명시적으로 연관 엔티티 로드
            Post loadedProduct = transaction.getPost();
            User buyer = transaction.getBuyer();
            User seller = transaction.getSeller();

            // DTO 생성
            return TransactionResponse.builder()
                    .id(transaction.getId())
                    .postId(loadedProduct.getId())
                    .productName(loadedProduct.getTitle())
                    .amount(loadedProduct.getPrice())
                    .buyerId(buyer.getId())
                    .buyerName(buyer.getUsername())
                    .sellerId(seller.getId())
                    .sellerName(seller.getUsername())
                    .isReceived(transaction.getIsReceived())
                    .receivedAt(transaction.getReceivedAt())
                    .status(transaction.getStatus())
                    .orderId(request.getOrderId())
                    .createdAt(transaction.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("결제 승인 실패: paymentKey={}, error={}", request.getPaymentKey(), e.getMessage());
            throw new RuntimeException("결제 승인에 실패했습니다: " + e.getMessage());
        }

    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String random = UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();

        return "ORDER_" + timestamp + "_" + random;
    }

}
