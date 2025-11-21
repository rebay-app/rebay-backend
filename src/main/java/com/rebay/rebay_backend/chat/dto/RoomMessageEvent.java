package com.rebay.rebay_backend.chat.dto;

import java.time.LocalDateTime;

public record RoomMessageEvent(
        Long roomId,
        Long messageId,
        Long senderId,
        String type,
        String content,
        LocalDateTime createdAt
) {}
