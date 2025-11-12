package com.rebay.rebay_backend.payment.config;

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.PaymentStatus;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.payment.repository.TransactionRepository;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Component    // 테스트 데이터 생성이 필요할 시 Component 주석 제거
@RequiredArgsConstructor
@Slf4j
@Profile("!test")  // 테스트 환경에서는 실행하지 않음
public class DataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 초기 데이터가 있으면 스킵
        if (paymentRepository.count() > 0) {
            log.info("✅ 초기 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("🔄 초기 더미 데이터 생성 중...");


        // 구매자 데이터
        User buyer = userRepository.findByUsername("buyer")
                .orElseGet(() -> {
                    User user = User.builder()
                            .username("buyer")
                            .email("buyer@example.com")
                            .password("password123")
                            .build();
                    return userRepository.save(user);
                });


        // 판매자 데이터
        User seller = userRepository.findByUsername("seller")
                .orElseGet(() -> {
                    User user = User.builder()
                            .username("seller")
                            .email("seller@example.com")
                            .password("password123")
                            .build();
                    return userRepository.save(user);
                });

        // 테스트용 상품
        Post post = postRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Post product = Post.builder()
                            .title("테스트 상품")
                            .content("더미 데이터용 테스트 상품")
                            .price(new BigDecimal("1000"))
                            .status(SaleStatus.ON_SALE)
                            .user(seller)
                            .build();
                    return postRepository.save(product);
                });

        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        Payment payment = Payment.builder()
                .transaction(transaction)
                .user(buyer)
                .orderId("ORDER_20251107_12345")
                .amount(post.getPrice())
                .paymentStatus(PaymentStatus.READY)
                .transactionStatus(TransactionStatus.PAYMENT_PENDING)
                .build();
        paymentRepository.save(payment);

        log.info("✅ 초기 더미 데이터 생성 완료");
        log.info("사용자: buyer={}, seller={}", buyer.getUsername(), seller.getUsername());
        log.info("상품: {}", post.getTitle());
        log.info("거래(Transaction) ID: {}, 결제(Payment) OrderID: {}", transaction.getId(), payment.getOrderId());
    }
}