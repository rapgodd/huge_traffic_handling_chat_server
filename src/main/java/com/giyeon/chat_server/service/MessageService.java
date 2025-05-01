package com.giyeon.chat_server.service;
import com.giyeon.chat_server.annotation.Sharding;

import com.giyeon.chat_server.entity.Message;
import com.giyeon.chat_server.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Sharding
    @Transactional
    public Message insertMessage(Long id) {
        Message message = new Message();
        message.setMessage("안녕하세요 " + id+"아이디의 메세지 입니다.");

        messageRepository.save(message);
        return message;
    }
}
