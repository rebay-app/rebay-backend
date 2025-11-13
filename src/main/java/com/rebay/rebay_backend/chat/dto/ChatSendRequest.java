package com.rebay.rebay_backend.chat.dto;

public record ChatSendRequest(
        String content,
        String type   // TEXT, IMAGE, SYSTEM
) {}
