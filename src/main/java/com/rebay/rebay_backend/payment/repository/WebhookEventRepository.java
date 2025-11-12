package com.rebay.rebay_backend.payment.repository;

import com.rebay.rebay_backend.payment.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    // eventId로 Webhook event 조회
    Optional<WebhookEvent> findByEventId(String eventId);

    // eventId 존재 여부 확인
    boolean existsByEventId(String eventId);
}
