package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.GrpcChatClient;
import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final SessionRegistry sessionRegistry;
    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    private final GrpcChatClient grpcClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public void sendMessage(Long myId, Long roomId, String payload) {
        // 메세지 저장
        messageRepositoryService.insertMessage(roomId,payload);

        // 방에 있는 유저들 조회
        RoomUsersDto usersInRoom = mainRepositoryService.getRoom(roomId);
        System.out.println("usersInRoom = " + usersInRoom+"\n");

        usersInRoom.getUserChatRoomList().forEach(userChatRoom -> {
            //방에 있는 모든 사람에게 메세지 전송
            if (sessionRegistry.isUserSessionExist(userChatRoom.getUser().getId())) {
                //userId로 sessionId를 찾고 && 전송
                WebSocketSession userSession = sessionRegistry.getUserSession(userChatRoom.getUser().getId());

                try {
                    userSession.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // 현재 HashMap에 상대 유저의 세션이 없다면 레디스를 통해 IP 획득 후
                String IP = redisTemplate.opsForValue().get(userChatRoom.getUser().getId().toString());
                System.out.println("IP = " + IP+"\n");
                // IP가 null이 아닐 경우 grpc 요청
                if(IP!=null){
                    // grpc 요청
                    // grpcClient.sendMessage(user.getId(), roomId, payload);
                    grpcClient.send(payload,IP.split(":")[0],Integer.parseInt(IP.split(":")[1]),userChatRoom.getUser().getId());
                }

            }
        });
    }

}
