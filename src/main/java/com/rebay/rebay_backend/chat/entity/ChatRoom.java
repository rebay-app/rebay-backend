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
        name = "chat_rooms",
        indexes = @Index(name = "ux_direct_pair",
                columnList = "participant1_id, participant2_id", unique = true)
)
public class ChatRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Setter
    @Column(name = "participant1_id", nullable = false)
    private Long participant1Id;

    @Setter
    @Column(name = "participant2_id", nullable = false)
    private Long participant2Id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Setter
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 개발 및 테스트용
    public static ChatRoom create(Long p1, Long p2) {
        ChatRoom r = new ChatRoom();
        r.participant1Id = p1;
        r.participant2Id = p2;
        return r;
    }

}