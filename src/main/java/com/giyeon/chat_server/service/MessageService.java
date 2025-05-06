package com.giyeon.chat_server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giyeon.chat_server.component.GrpcChatClient;
import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.service.MainRepositoryService;
import com.giyeon.chat_server.service.MessageRepositoryService;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@RequiredArgsConstructor
public class MessageService{

    private final SessionRegistry sessionRegistry;
    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    private final GrpcChatClient grpcClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ThreadPoolExecutor threadPoolExecutor;


    public void sendMessage(ChatDto chatDto) {

        Long roomId = chatDto.getRoomId();
        String payload = chatDto.getMessage();


        messageRepositoryService.insertMessage(roomId,payload);

        threadPoolExecutor.submit(()->{
            String json;
            try {
                json = objectMapper.writeValueAsString(chatDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            RoomUsersDto usersInRoom = mainRepositoryService.getRoom(roomId);
            usersInRoom.getUserChatRoomList().forEach(userChatRoom -> {

                if (sessionRegistry.isUserSessionExist(userChatRoom.getUser().getId())) {

                    WebSocketSession userSession = sessionRegistry.getUserSession(userChatRoom.getUser().getId());
                    try {
                        userSession.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {

                    String IP = redisTemplate.opsForValue().get(userChatRoom.getUser().getId().toString());
                    System.out.println("IP = " + IP+"\n");

                    if(IP!=null){
                        grpcClient.send(json,IP.split(":")[0],Integer.parseInt(IP.split(":")[1]),userChatRoom.getUser().getId());
                    }

                }
            });

        });

    }

}
