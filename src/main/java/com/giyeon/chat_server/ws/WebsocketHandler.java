package com.giyeon.chat_server.ws;


import com.giyeon.chat_server.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

    private final MessageService messageService;
    private final SessionRegistry sessionRegistry;
    private final Environment environment;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message){
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("websocket connected!. sessionId={}", session.getId());

        //웹소켓 연결로 바뀐 후에 연결이 지속되는 동안 사용자 id:세션, Id:ip 저장
        sessionRegistry.setUserSession(Long.valueOf(environment.getProperty("user.id")), session);
        redisTemplate.opsForValue().set(environment.getProperty("user.id"),environment.getProperty("grpc.ip")+":"+environment.getProperty("grpc.port"));
        System.out.println(environment.getProperty("user.id"+"\n"));

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }



}
