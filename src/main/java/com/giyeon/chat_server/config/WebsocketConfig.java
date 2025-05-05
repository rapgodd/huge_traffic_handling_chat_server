package com.giyeon.chat_server.config;

import com.giyeon.chat_server.ws.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@RequiredArgsConstructor
@Configuration
public class WebsocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final Environment environment;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/mypath/{roomId}")
                .addInterceptors(new AuthHandshakeInterceptor(environment))
                .setAllowedOrigins("*");
    }


}
