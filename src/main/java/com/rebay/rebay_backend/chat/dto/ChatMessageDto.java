package com.rebay.rebay_backend.chat.dto;


import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String roomId;
    private String sender;
    private String sessionId;
    private String targetSessionId;
    private String message;
    private MessageType type;
    private LocalDateTime createdAt;

    public enum MessageType { CHAT, JOIN, LEAVE }
}