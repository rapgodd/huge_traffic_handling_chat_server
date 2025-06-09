package com.giyeon.chat_server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.exception.customException.MsgDataSaveException;
import com.giyeon.chat_server.service.msgSender.remoteSender.RemoteSessionMessageSender;
import com.giyeon.chat_server.service.redisService.RedisService;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.IdDistributionUtils;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService{

    private final SessionRegistry sessionRegistry;
    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final RemoteSessionMessageSender remoteSessionMessageSender;
    @Autowired
    private RedisService redisService;

    @Retryable(
            value = {MsgDataSaveException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public void sendMessage(ChatDto chatDto) {

        Long roomId = chatDto.getRoomId();
        String payload = chatDto.getMessage();
        Long senderId = chatDto.getSenderId();
        ZonedDateTime createdAt = ZonedDateTime.now();
        Long msgId;

        try {
            msgId = messageRepositoryService.insertMessage(roomId, payload, senderId, createdAt);
        } catch (RuntimeException e) {
            throw MsgDataSaveException.EXCEPTION;
        }

        threadPoolExecutor.submit(()->{
            String json;
            List<Long> currentJoinedUsers = redisService.getCurrentJoinedUsers(roomId);
            chatDto.setJoinedUser(currentJoinedUsers.size());
            try {
                json = objectMapper.writeValueAsString(chatDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            RoomUsersDto usersInRoom = mainRepositoryService.getRoom(roomId);

            List<Long> localSessionUsersList = new ArrayList<>(30);
            List<Long> remoteSessionUsersList = new ArrayList<>(30);
            List<Long> userIds = new ArrayList<>(30);


            usersInRoom.getUserChatRoomList().forEach(userChatRoom -> {
                userIds.add(userChatRoom.getUser().getId());
            });

            IdDistributionUtils.distributeRemoteAndLocal(userIds, localSessionUsersList,
                                remoteSessionUsersList, sessionRegistry, redisTemplate);

            sendJoinedLocalUsers(localSessionUsersList, json);
            sendJoinedRemoteUsers(remoteSessionUsersList, roomId, json);

            // Redis 에 저장된 방의 마지막 메시지 ID 를 업데이트
            redisService.putUsersLastMsgIdInRoom(currentJoinedUsers, roomId, String.valueOf(msgId));
            redisService.putLastMsgIdInRoom(roomId, String.valueOf(msgId));

        });

    }

    @Recover
    public void recover(MsgDataSaveException e, ChatDto chatDto) {
        log.error("sendMessage 재시도 모두 실패: {}", e.getMessage(), e);
        throw new RuntimeException("메시지 전송 실패, 잠시 후 다시 시도해 주세요", e);
    }


    private void sendJoinedRemoteUsers(List<Long> remoteSessionUsersList, Long roomId, String json) {
        for (Long id : remoteSessionUsersList) {
            String IP = (String)redisTemplate.opsForValue().get(String.valueOf(id));
            if(IP!=null){
                remoteSessionMessageSender.addMessageToQueue(json,IP.split(":")[0],Integer.parseInt(IP.split(":")[1]), roomId,id);
            }
        }
    }

    private void sendJoinedLocalUsers(List<Long> localSessionUsersList, String json) {
        for (Long id : localSessionUsersList) {
            try {
                sessionRegistry.getUserSession(id).sendMessage(new TextMessage(json));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
