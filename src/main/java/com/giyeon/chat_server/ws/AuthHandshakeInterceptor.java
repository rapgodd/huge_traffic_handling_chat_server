package com.giyeon.chat_server.ws;

import com.giyeon.chat_server.properties.DataSourceProperty;
import com.giyeon.chat_server.properties.JwtProperty;
import com.giyeon.chat_server.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProperty jwtProperty;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 파라미터에 ws/mypath/{token}이 들어오면 토큰 추출하여서 유저 ID 파싱
        String path = request.getURI().getPath();
        String[] segments = path.split("/");
        if(segments.length==4){
            String token = segments[segments.length - 1];
            Claims claim = JwtUtil.parseClaims(token, jwtProperty.getSecret());
            String userId = claim.get("sub", String.class);
            attributes.put("userId", userId);
            return true;
        }else{
            return false;
        }

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
