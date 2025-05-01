package com.giyeon.chat_server.repository;


import com.giyeon.chat_server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message,Long> {
}
