package com.giyeon.chat_server.ws;

import com.giyeon.chat_server.properties.GrpcProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {

    private final SessionRegistry sessionRegistry;
    private final GrpcProperty grpcProperty;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message){
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("websocket connected!. sessionId={}", session.getId());

        //HashMap에 userId, roomId 저장
        //레디스에 userId, ip:port 저장
        String userId = (String)session.getAttributes().get("userId");
        redisTemplate.opsForValue().set(userId,grpcProperty.getIp()+":"+grpcProperty.getPort());
        sessionRegistry.setUserSession(Long.valueOf(userId), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("websocket disconnected!. sessionId={}", session.getId());
        Long userId = Long.valueOf((String) session.getAttributes().get("userId"));
        //HashMap에서 userId, roomId 삭제
        sessionRegistry.removeUserSession(userId);
        //레디스에서 userId, ip:port 삭제
        redisTemplate.delete(String.valueOf(userId));
        super.afterConnectionClosed(session, status);
    }



}
