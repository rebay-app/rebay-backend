package com.rebay.rebay_backend.repository;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// test는 h2 인메모리로 진행
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository productRepository;

    private User buyer;
    private User seller;
    private Post post;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        buyer = User.builder()
                .username("구매자")
                .email("buyer@test.com")
                .password("password")
                .build();
        userRepository.save(buyer);

        seller = User.builder()
                .username("판매자")
                .email("seller@test.com")
                .password("password")
                .build();
        userRepository.save(seller);

        post = Post.builder()
                .title("테스트 상품")
                .content("테스트용 상품입니다")
                .price(BigDecimal.valueOf(10000))
                .user(seller)
                .status(SaleStatus.ON_SALE)
                .build();
        productRepository.save(post);

        entityManager.flush();
    }

    @Test
    @DisplayName("Payment 저장 및 조회 테스트")
    void payment_저장_조회_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();
        transactionRepository.save(transaction);

        Payment payment = Payment.create(transaction, "ORDER_TEST123", BigDecimal.valueOf(10000));

        // When
        Payment savedPayment = paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Payment> found = paymentRepository.findById(savedPayment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo("ORDER_TEST123");
        assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(found.get().getPaymentStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("OrderId로 Payment 조회 테스트")
    void payment_orderId로_조회_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();
        transactionRepository.save(transaction);

        String orderId = "ORDER_UNIQUE_123";
        Payment payment = Payment.create(transaction, orderId, BigDecimal.valueOf(10000));
        paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Payment> found = paymentRepository.findByOrderId(orderId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("TransactionId로 Payment 조회 테스트")
    void payment_transactionId로_조회_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        Payment payment = Payment.create(savedTransaction, "ORDER_TEST456", BigDecimal.valueOf(20000));
        paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Payment> found = paymentRepository.findByTransactionId(savedTransaction.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("Payment 상태 업데이트 테스트")
    void payment_상태_업데이트_테스트() {
        // Given
        Transaction transaction = Transaction.builder()
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();
        transactionRepository.save(transaction);

        Payment payment = Payment.create(transaction, "ORDER_TEST789", BigDecimal.valueOf(30000));
        Payment savedPayment = paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();

        // When
        Payment foundPayment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        foundPayment.approve("payment_key_123", "카드", "https://receipt.url");
        paymentRepository.save(foundPayment);
        entityManager.flush();
        entityManager.clear();

        // Then
        Payment updatedPayment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(updatedPayment.getPaymentKey()).isEqualTo("payment_key_123");
        assertThat(updatedPayment.getMethod()).isEqualTo("카드");
        assertThat(updatedPayment.getApprovedAt()).isNotNull();
    }
}