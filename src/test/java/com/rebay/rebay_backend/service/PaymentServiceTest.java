package com.rebay.rebay_backend.service;

// MOCKITO를 사용하여 의존성을 Mock으로 대체
// 비즈니스 로직만 테스트

import com.rebay.rebay_backend.Post.entity.Post;
import com.rebay.rebay_backend.Post.entity.SaleStatus;
import com.rebay.rebay_backend.Post.repository.PostRepository;
import com.rebay.rebay_backend.payment.dto.PaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentRequest;
import com.rebay.rebay_backend.payment.dto.TossPaymentResponse;
import com.rebay.rebay_backend.payment.dto.TransactionResponse;
import com.rebay.rebay_backend.payment.entity.Payment;
import com.rebay.rebay_backend.payment.entity.PaymentStatus;
import com.rebay.rebay_backend.payment.entity.Transaction;
import com.rebay.rebay_backend.payment.entity.TransactionStatus;
import com.rebay.rebay_backend.payment.repository.PaymentRepository;
import com.rebay.rebay_backend.payment.repository.TransactionRepository;
import com.rebay.rebay_backend.payment.service.PaymentService;
import com.rebay.rebay_backend.payment.service.TossPaymentsApiClient;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TossPaymentsApiClient tossPaymentsApiClient;

    @InjectMocks
    private PaymentService paymentService;

    private User buyer;
    private User seller;
    private Post post;

    private Transaction transaction;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        buyer = User.builder()
                .id(1L)
                .username("구매합니다")
                .build();

        seller = User.builder()
                .id(2L)
                .username("판매합니다")
                .build();

        post = Post.builder()
                .id(1L)
                .title("테스트 상품입니다")
                .price(BigDecimal.valueOf(10000))
                .user(seller)
                .status(SaleStatus.ON_SALE)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAYMENT_PENDING)
                .isReceived(false)
                .build();

        payment = Payment.builder()
                .id(1L)
                .transaction(transaction)
                .orderId("ORDER_251110_123")
                .amount(BigDecimal.valueOf(10000))
                .paymentStatus(PaymentStatus.READY)
                .build();
    }

    @Test
    @DisplayName("결제 준비 테스트")
    void Payment_준비_테스트() {
        // Given (테스트 준비)
        PaymentRequest request = new PaymentRequest();
        request.setPostId(1L);
        request.setBuyerId(1L);
        request.setAmount(10000);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When (실행)
        TransactionResponse response = paymentService.preparePayment(request);

        // Then (검증)
        assertThat(response).isNotNull();
        assertThat(response.getPostId()).isEqualTo(1L);
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getSellerId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PAYMENT_PENDING);

        verify(postRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));

    }

    @Test
    @DisplayName("결제 준비 실패 - 상품 없음")
    void payment_실패_상품없음() {
        // Given (테스트 준비)
        PaymentRequest request = new PaymentRequest();
        request.setPostId(999L);
        request.setBuyerId(1L);

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then (실행 & 검증)
        assertThatThrownBy(() -> paymentService.preparePayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("결제 승인 테스트")
    void payment_승인_테스트() {
        // Given
        TossPaymentRequest request = new TossPaymentRequest();
        request.setPaymentKey("test_payment_key");
        request.setOrderId("ORDER_251110_123");
        request.setAmount(10000);

        TossPaymentResponse tossPaymentResponse = new TossPaymentResponse();
        tossPaymentResponse.setPaymentKey(request.getPaymentKey());
        tossPaymentResponse.setOrderId(request.getOrderId());
        tossPaymentResponse.setMethod("카드");
        tossPaymentResponse.setTotalAmount(request.getAmount());
        tossPaymentResponse.setStatus("DONE");

        TossPaymentResponse.Receipt receipt = new TossPaymentResponse.Receipt();
        receipt.setUrl("https://receipt.url");
        tossPaymentResponse.setReceipt(receipt);

        when(paymentRepository.findByOrderId(request.getOrderId())).thenReturn(Optional.of(payment));
        when(tossPaymentsApiClient.confirmPayment(request)).thenReturn(tossPaymentResponse);

        // When
        TransactionResponse response = paymentService.confirmPayment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PAID);

        verify(paymentRepository, times(1)).findByOrderId(request.getOrderId());
        verify(tossPaymentsApiClient, times(1)).confirmPayment(request);

    }

    @Test
    @DisplayName("결제 실패 - 금액 불일치")
    void payment_결제_실패_금액불일치() {
        // Given
        TossPaymentRequest request = new TossPaymentRequest();
        request.setPaymentKey("test_payment_key");
        request.setOrderId("ORDER_251110_123");
        request.setAmount(100);

        when(paymentRepository.findByOrderId(request.getOrderId())).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액이 일치하지 않습니다");

        verify(paymentRepository, times(1)).findByOrderId(request.getOrderId());
        verify(tossPaymentsApiClient, never()).confirmPayment(any());
    }

    @Test
    @DisplayName("거래 완료 확인 테스트")
    void payment_거래완료_확인_테스트() {
        // Given
        Long transactionId = 1L;
        Long buyerId = 1L;

        transaction = Transaction.builder()
                .id(transactionId)
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAID)
                .isReceived(false)
                .build();

        payment = Payment.builder()
                .id(1L)
                .transaction(transaction)
                .orderId("ORDER_TEST123")
                .amount(BigDecimal.valueOf(10000))
                .paymentStatus(PaymentStatus.DONE)
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(paymentRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(payment));

        // When
        TransactionResponse response = paymentService.confirmReceipt(transactionId, buyerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(paymentRepository, times(1)).findByTransactionId(transactionId);

    }

    @Test
    @DisplayName("거래 완료 실패 - 구매자 불일치")
    void payment_거래완료_실패_구매자불일치() {
        // Given
        Long transactionId = 1L;
        Long wrongBuyerId = 999L;

        transaction = Transaction.builder()
                .id(transactionId)
                .post(post)
                .buyer(buyer)
                .seller(seller)
                .status(TransactionStatus.PAID)
                .isReceived(false)
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // When & Then
        assertThatThrownBy(() -> paymentService.confirmReceipt(transactionId, wrongBuyerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("구매자만 상품 수령을 확인할 수 있습니다");

        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, never()).save(any());
    }

}
