package com.giyeon.chat_server.repository.message;

import com.giyeon.chat_server.entity.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByRoomId(Long roomId);
}
