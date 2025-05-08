package com.giyeon.chat_server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public final class JwtUtil {




    public static String createAccessToken(Long userId, List<String> roles, byte[] bytes, Long ACCESS_TOKEN_EXPIRATION_TIME) {
        return Jwts.builder()
                // subject = userId
                .setSubject(String.valueOf(userId))
                // claim = 내용으로 권한을 삽입했습니다.
                .claim("role", roles)
                // 발급 시간
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // 만료 시간
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(Keys.hmacShaKeyFor(bytes), SignatureAlgorithm.HS512)
                .compact();
    }

    public static String createRefreshToken(Long userId, byte[] bytes, Long REFRESH_TOKEN_EXPIRATION_TIME) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(Keys.hmacShaKeyFor(bytes), SignatureAlgorithm.HS512)
                .compact();
    }

    public static Claims getHandledClaim(String token, byte[] secretBytes) {
        Claims claims;
        try {
            claims = extract(token, secretBytes);
        }catch (Exception e){
            throw new RuntimeException("JWT 토큰 검증과정에서 오류가 발생했습니다.");
        }
        return claims;
    }

    public static Claims extract(String token, byte[] secretBytes) {
        Claims claims;
        claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    public static Long getUserId(String JWT_SECRET_KEY) {
        String accessToken = JwtUtil.getAccessToken();
        Claims body = JwtUtil.parseClaims(accessToken, JWT_SECRET_KEY);
        String id = body.get("sub", String.class);

        return Long.valueOf(id);
    }

    public static String getAccessToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        else {
            throw new JwtException("JWT 토큰이 없습니다.");
        }

        return token;
    }

    public static Claims parseClaims(String token, String JWT_SECRET_KEY){
        try{
            return Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }


}