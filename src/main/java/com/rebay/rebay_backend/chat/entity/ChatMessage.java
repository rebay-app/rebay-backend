package com.rebay.rebay_backend.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_msg_room_id", columnList = "room_id"),
                @Index(name = "idx_msg_room_message", columnList = "room_id, message_id")
        }
)
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Setter
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Setter
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @Setter
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (messageType == null) messageType = MessageType.TEXT; // 타입 기본값 방어
        if (createdAt == null) createdAt = LocalDateTime.now();  // 시간 기본값 방어
    }

    @Builder
    private ChatMessage(Long roomId, Long senderId, MessageType messageType, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.messageType = (messageType != null ? messageType : MessageType.TEXT);
        this.content = content;
    }

    public enum MessageType { TEXT, IMAGE, SYSTEM }
}
