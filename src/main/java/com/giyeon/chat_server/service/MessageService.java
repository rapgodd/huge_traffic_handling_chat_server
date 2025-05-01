package com.giyeon.chat_server.service;

import com.giyeon.chat_server.annotation.Sharding;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public Message insertMessage(Long id) {
        Message message = new Message();
        message.setRoomId(id);

        messageRepository.save(message);
        return message;
    }

}
