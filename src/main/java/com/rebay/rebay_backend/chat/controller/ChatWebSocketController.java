package com.rebay.rebay_backend.chat.controller;

import com.rebay.rebay_backend.chat.dto.ChatSendRequest;
import com.rebay.rebay_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;

    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId,
                     @Payload ChatSendRequest req,
                     Principal principal,
                     @Header(name = "user-id", required = false) String userHeader) {

        Long senderId = null;
        if (principal != null && principal.getName() != null) {
            try { senderId = Long.parseLong(principal.getName()); } catch (Exception ignored) {}
        }
        if (senderId == null && userHeader != null) {
            try { senderId = Long.parseLong(userHeader); } catch (Exception ignored) {}
        }
        if (senderId == null) throw new RuntimeException("Unauthenticated (Principal or 'user-id' header)");

        chatService.send(roomId, senderId, req);
    }
}
