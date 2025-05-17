package com.giyeon.chat_server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.service.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
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
    private RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ThreadSendingService threadSendingService;

    public void sendMessage(ChatDto chatDto) {

        Long roomId = chatDto.getRoomId();
        String payload = chatDto.getMessage();
        Long senderId = chatDto.getSenderId();
        ZonedDateTime createdAt = chatDto.getCreatedAt();

        messageRepositoryService.insertMessage(roomId,payload,senderId,createdAt);

        threadPoolExecutor.submit(()->{
            String json;
            try {
                json = objectMapper.writeValueAsString(chatDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            RoomUsersDto usersInRoom = mainRepositoryService.getRoom(roomId);

            List<Long> localSessionUsersList = new ArrayList<>(30);
            List<Long> remoteSessionUsersList = new ArrayList<>(30);


            usersInRoom.getUserChatRoomList().forEach(userChatRoom -> {
                Long userId = userChatRoom.getUser().getId();
                if (sessionRegistry.isUserSessionExist(userId)) {
                      localSessionUsersList.add(userId);


                } else {

                    String IP = redisTemplate.opsForValue().get(String.valueOf(userId));

                    if(IP!=null){
                        remoteSessionUsersList.add(userId);
                    }

                }
            });
            sendJoinedLocalUsers(localSessionUsersList, roomId, json);
            sendJoinedRemoteUsers(remoteSessionUsersList, roomId, json);

        });

    }

    private void sendJoinedRemoteUsers(List<Long> remoteSessionUsersList, Long roomId, String json) {
        List<Long> joinedUsersInCurrentRoom = mainRepositoryService.findJoinedUsersInCurrentRoom(remoteSessionUsersList, roomId);
        for (Long id : joinedUsersInCurrentRoom) {
            String IP = redisTemplate.opsForValue().get(String.valueOf(id));
            if(IP!=null){
                threadSendingService.addMessageToQueue(json,IP.split(":")[0],Integer.parseInt(IP.split(":")[1]), roomId,id);
            }
        }
    }

    private void sendJoinedLocalUsers(List<Long> localSessionUsersList, Long roomId, String json) {
        List<Long> joinedUsersId = mainRepositoryService.findJoinedUsersInCurrentRoom(localSessionUsersList, roomId);
        for (Long id : joinedUsersId) {
            try {
                sessionRegistry.getUserSession(id).sendMessage(new TextMessage(json));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
