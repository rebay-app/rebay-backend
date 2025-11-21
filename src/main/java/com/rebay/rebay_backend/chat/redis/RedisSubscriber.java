package com.rebay.rebay_backend.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebay.rebay_backend.chat.dto.RoomMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate template;
    private final ObjectMapper om;

    @Override
    public void onMessage(Message msg, byte[] pattern) {
        try {
            String body = new String(msg.getBody(), StandardCharsets.UTF_8);
            RoomMessageEvent ev = om.readValue(body, RoomMessageEvent.class);
            template.convertAndSend("/topic/rooms/" + ev.roomId(), ev);
        } catch (Exception e) {
            log.warn("Redis handle failed: {}", e.toString(), e);
        }
    }
}
