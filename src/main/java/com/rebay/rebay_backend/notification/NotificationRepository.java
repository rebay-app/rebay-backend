package com.rebay.rebay_backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop30ByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    long countByReceiverIdAndReadIsFalse(Long receiverId);
}