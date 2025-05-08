package com.giyeon.chat_server.repository.message;

import com.giyeon.chat_server.entity.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByRoomId(Long roomId);

    List<Message> findByRoomIdAndCreatedAtGreaterThan(Long roomId, LocalDateTime leavedAt);

    //채팅방 안 가장 마지막으로 보낸 메세지만 출력
    Optional<Message> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);
}
