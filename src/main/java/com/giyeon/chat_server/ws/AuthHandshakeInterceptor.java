package com.giyeon.chat_server.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final Environment environment;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 방 ID 저장
        String path = request.getURI().getPath();                // "/ws/chat/123"
        String[] segments = path.split("/");               // ["", "ws", "chat", "123"]

        if(segments.length==4){
            String roomIdStr = segments[segments.length - 1];        // "123"
            Long roomId = Long.valueOf(roomIdStr);
            attributes.put("roomId", roomId);
            attributes.put("userId", Long.valueOf(environment.getProperty("user.id")));
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
