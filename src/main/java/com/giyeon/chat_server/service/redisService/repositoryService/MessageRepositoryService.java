package com.giyeon.chat_server.service.redisService.repositoryService;

import com.giyeon.chat_server.annotation.Sharding;
import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.MessageJdbcDto;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.message.MessageJdbcRepository;
import com.giyeon.chat_server.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageRepositoryService {

    private final MessageRepository messageRepository;
    private final IdGenerator idGenerator;
    private final MessageJdbcRepository messageJdbcRepository;

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public Long insertMessage(Long roomId, String message, Long senderId, ZonedDateTime createdAt, String imageUrl) {

        Long id = idGenerator.nextId();
        Message userMessage = Message.builder()
                .id(id)
                .message(message)
                .roomId(roomId)
                .senderId(senderId)
                .createdAt(createdAt)
                .imageUrl(imageUrl)
                .build();

        messageRepository.save(userMessage);
        return id;
    }

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public List<Message> getMessages(Long roomId, Pageable pageable) {

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public List<Message> getMessagesGreaterThan(Long roomId, LocalDateTime leavedAt) {
        List<Message> unreadMessages = messageRepository.findByRoomIdAndCreatedAtGreaterThan(roomId, leavedAt);

        if(unreadMessages.isEmpty()){

            Message message = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId).orElse(
                    Message.builder()
                            .message(" ")
                            .roomId(roomId)
                            .build()
            );
            return List.of(message);

        }else{
            return unreadMessages;
        }

    }

    @Sharding
    @Transactional(value = "jdbcTransactionManager")
    public List<MessageJdbcDto> getAggregates(Long roomId, HashMap<Long, Long> roomAndLastMsgId) {
        List<MessageJdbcDto> messageJdbcDtos = messageJdbcRepository.fetchAggregates(roomAndLastMsgId);

        return messageJdbcDtos;
    }

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public void save(Long roomId, Message msg) {
        messageRepository.save(msg);
    }

    @Sharding
    @Transactional(value = "messagePlatformTransactionManager")
    public void deleteAll(Long roomId){
        messageRepository.deleteAll();
    }
}
