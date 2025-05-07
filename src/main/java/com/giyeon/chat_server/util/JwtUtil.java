package com.giyeon.chat_server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.List;

public final class JwtUtil {

    private JwtUtil() {

    }

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


}