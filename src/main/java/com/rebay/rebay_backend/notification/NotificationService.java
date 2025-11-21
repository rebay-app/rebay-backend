package com.rebay.rebay_backend.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    public Notification notify(Long receiverId, String type, String message, String link) {
        Notification n = new Notification();
        n.setReceiverId(receiverId);
        n.setType(type);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(n);

        // 실시간 전송
        sseService.sendTo(receiverId, saved);

        return saved;
    }

    public void markRead(Long id, Long userId) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found"));
        if (!n.getReceiverId().equals(userId)) {
            throw new RuntimeException("no permission");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }
}
