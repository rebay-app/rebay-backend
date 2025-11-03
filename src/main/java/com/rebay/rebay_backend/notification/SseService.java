package com.rebay.rebay_backend.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {

    private static final long TIMEOUT = 60L * 1000 * 60; // 1시간
    private final EmitterRepository repo;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        repo.save(userId, emitter);

        emitter.onCompletion(() -> repo.delete(userId));
        emitter.onTimeout(() -> repo.delete(userId));
        emitter.onError((e) -> repo.delete(userId));

        // 연결 확인용
        sendTo(userId, "connected");

        return emitter;
    }

    public void sendTo(Long userId, Object data) {
        SseEmitter emitter = repo.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (Exception e) {
                repo.delete(userId);
            }
        }
    }
}
