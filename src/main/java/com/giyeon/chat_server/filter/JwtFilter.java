package com.giyeon.chat_server.filter;


import com.giyeon.chat_server.config.SecurityConfig;
import com.giyeon.chat_server.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JwtFilter extends OncePerRequestFilter {
    private final String JWT_SECRET_KEY;

    public JwtFilter(String jwtSecretKey) {
        this.JWT_SECRET_KEY = jwtSecretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header  = request.getHeader("Authorization");

        //

        extractToken(header).ifPresentOrElse(token -> {
            byte[] secretBytes = JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8);

            Claims claims = JwtUtil.getHandledClaim(token, secretBytes);
            createSession(claims);

            System.out.println("JWT 인증 통과");
            handOverToNext(request, response, filterChain);
        }, () -> {
            throw new RuntimeException("JWT 토큰이 존재하지 않습니다.");
        });

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String path = request.getServletPath();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        for (String s : SecurityConfig.PERMIT_URLS) {
            if (pathMatcher.match(s, path)) {
                return true;
            }
        }
        return false;

    }


    private void handOverToNext(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        }catch (Exception e){
            throw new RuntimeException("JWT필터 다음 필터 체인 시도가 실패했습니다.");
        }
    }

    private void createSession(Claims claims) {
        String email = claims.getSubject();
        String role = String.valueOf(claims.get("role"));

        // 사용자 정보를 기반으로 Authentication 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(email, "", AuthorityUtils.commaSeparatedStringToAuthorityList(role));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    private Optional<String> extractToken(String token) {
        if(StringUtils.hasText(token)&& token.startsWith("Bearer")){
            return Optional.ofNullable(token.substring(7));
        }else{
            return Optional.ofNullable(null);
        }
    }


}
