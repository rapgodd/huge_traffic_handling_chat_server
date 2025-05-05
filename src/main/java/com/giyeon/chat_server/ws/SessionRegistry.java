package com.giyeon.chat_server.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private ConcurrentHashMap<Long, WebSocketSession> userIdSessionMap = new ConcurrentHashMap<>(3000);

    public WebSocketSession getUserSession(Long userId){
        return userIdSessionMap.get(userId);
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
