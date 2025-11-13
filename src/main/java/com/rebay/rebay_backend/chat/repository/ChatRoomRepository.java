package com.rebay.rebay_backend.chat.repository;


import com.rebay.rebay_backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
           select case when count(r)>0 then true else false end
           from ChatRoom r
           where r.id = :roomId
             and (r.participant1Id = :userId or r.participant2Id = :userId)
           """)
    boolean existsByIdAndParticipant(Long roomId, Long userId);
}