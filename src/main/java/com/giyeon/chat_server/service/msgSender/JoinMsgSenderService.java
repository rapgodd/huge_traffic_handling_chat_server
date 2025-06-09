package com.giyeon.chat_server.service.msgSender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giyeon.chat_server.dto.JoinNoticeDto;
import com.giyeon.chat_server.service.msgSender.remoteSender.RemoteSessionMessageSender;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JoinMsgSenderService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RemoteSessionMessageSender remoteSessionMessageSender;
    private final ObjectMapper objectMapper;
    private final SessionRegistry sessionRegistry;


    public void sendJoinMsgToRemote(List<Long> remoteSessionUsersList, Long roomId, Long userId, Long lastMsgId) {
        for (Long id : remoteSessionUsersList) {
            String IP = (String) redisTemplate.opsForValue().get(String.valueOf(id));

            if (IP != null) {
                try {
                    remoteSessionMessageSender.addMessageToQueue(objectMapper.writeValueAsString(JoinNoticeDto.builder()
                            .userId(userId)
                            .lastMessageId(lastMsgId)
                            .build()), IP.split(":")[0], Integer.parseInt(IP.split(":")[1]), roomId, id);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendJoinMsgToLocal(List<Long> localSessionUsersList, Long userId, Long lastMsgId) {
        for (Long id : localSessionUsersList) {
            try {
                sessionRegistry.getUserSession(id).sendMessage(new TextMessage(objectMapper.writeValueAsBytes(JoinNoticeDto.builder()
                        .userId(userId)
                        .lastMessageId(lastMsgId)
                        .build())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
