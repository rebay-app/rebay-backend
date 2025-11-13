package com.rebay.rebay_backend.chat.service;

import com.rebay.rebay_backend.chat.dto.ChatSendRequest;
import com.rebay.rebay_backend.chat.dto.RoomMessageEvent;
import com.rebay.rebay_backend.chat.entity.ChatMessage;
import com.rebay.rebay_backend.chat.entity.ChatMessage.MessageType;
import com.rebay.rebay_backend.chat.redis.RedisPublisher;
import com.rebay.rebay_backend.chat.repository.ChatMessageRepository;
import com.rebay.rebay_backend.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisPublisher redisPublisher;

    @Transactional
    public void send(Long roomId, Long senderId, ChatSendRequest req) {
        if (!chatRoomRepository.existsByIdAndParticipant(roomId, senderId)) {
            throw new AccessDeniedException("Not a participant of room " + roomId);
        }

        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .roomId(roomId)
                        .senderId(senderId)
                        .messageType(parseType(req.type()))
                        .content(req.content())
                        .build()
        );

        RoomMessageEvent ev = new RoomMessageEvent(
                saved.getRoomId(),
                saved.getId(),
                saved.getSenderId(),
                saved.getMessageType().name(),
                saved.getContent(),
                saved.getCreatedAt()
        );
        redisPublisher.publishRoom(roomId, ev);
    }

    private static MessageType parseType(String type) {
        if (type == null) return MessageType.TEXT;
        try { return MessageType.valueOf(type.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return MessageType.TEXT; }
    }
}
