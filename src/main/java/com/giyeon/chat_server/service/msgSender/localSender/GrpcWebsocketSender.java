package com.giyeon.chat_server.service.msgSender.localSender;

import com.giyeon.chat_server.exception.customException.SessionSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcWebsocketSender {

    @Retryable(
            value = SessionSendException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public void sendToWebSocket(Long userId, String content, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(content));
            log.debug("WebSocket 전송 성공. userId={}", userId);
        } catch (IOException ex) {
            log.error("WebSocket 전송 중 IOException 발생. userId={}", userId, ex);
            throw SessionSendException.EXCEPTION;
        }
    }

    @Recover
    public void recover(SessionSendException cause, Long userId, String content, WebSocketSession session) {
        log.error("[Recover] WebSocket 전송 2회 모두 실패. userId={}, content={}", userId, content, cause);
        throw cause;
    }
}