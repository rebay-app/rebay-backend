package com.rebay.rebay_backend.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebay.rebay_backend.chat.dto.RoomMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {
    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public void publishRoom(Long roomId, RoomMessageEvent ev) {
        try {
            String channel = "chat.room." + roomId;
            String json = om.writeValueAsString(ev);
            Long delivered = redis.convertAndSend(channel, json);
            if (delivered == null || delivered == 0) {
                log.debug("No active Redis subscribers for {}", channel);
            }
        } catch (Exception e) {
            log.warn("Redis publish failed (room {}): {}", roomId, e.toString(), e);
        }
    }
}
