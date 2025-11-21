package com.rebay.rebay_backend.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue
    private Long id;

    private Long receiverId;     // 알림 받을 유저
    private String type;         // COMMENT, FOLLOW, BID, SYSTEM..
    private String message;      // 표시용 메시지
    private String link;         // 클릭 시 이동할 URL

    private boolean read;        // 읽음 여부

    private LocalDateTime createdAt;
}