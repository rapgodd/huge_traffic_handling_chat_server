package com.giyeon.chat_server.ws;

import com.giyeon.chat_server.exception.customException.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionRegistry {

    private ConcurrentHashMap<Long, WebSocketSession> userIdSessionMap = new ConcurrentHashMap<>(3000);

    public WebSocketSession getUserSession(Long userId){
        try {
            return userIdSessionMap.computeIfAbsent(userId, k -> null);
        }catch (Exception e) {
            log.error("Error retrieving WebSocket session for userId: {}", userId, e);
            throw NotFoundException.EXCEPTION;
        }
    }

    public void setUserSession(Long userId,WebSocketSession webSocketSession){
        userIdSessionMap.put(userId, webSocketSession);
    }

    public void removeUserSession(Long userId){
        userIdSessionMap.remove(userId);
    }

    public boolean isUserSessionExist(Long userId){
        return userIdSessionMap.containsKey(userId);
    }

}
