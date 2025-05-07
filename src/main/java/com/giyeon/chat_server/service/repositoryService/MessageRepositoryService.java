package com.giyeon.chat_server.service.repositoryService;

import com.giyeon.chat_server.annotation.Sharding;
import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.RoomMessagesDto;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageRepositoryService {

    private final MessageRepository messageRepository;
    private final IdGenerator idGenerator;

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public void insertMessage(Long roomId, String message) {

        Message userMessage = Message.builder()
                .id(idGenerator.nextId())
                .message(message)
                .roomId(roomId)
                .build();

        messageRepository.save(userMessage);
        System.out.println("insertMessage: " + userMessage);
    }

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public List<RoomMessagesDto> getMessages(Long roomId) {
        List<RoomMessagesDto> messageDtoList = new ArrayList<>();

        messageRepository.findByRoomId(roomId).forEach(msg->{
            messageDtoList.add(RoomMessagesDto.builder()
                    .message(msg.getMessage())
                    .sender("giyeon")
                    .build());
        });

        return messageDtoList;
    }
}
