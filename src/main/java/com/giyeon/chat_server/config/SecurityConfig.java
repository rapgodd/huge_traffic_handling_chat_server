package com.giyeon.chat_server.config;

import com.giyeon.chat_server.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static String JWT_SECRET_KEY;
    public static final String[] PERMIT_URLS = {
            "/api/login",
            "/api/refresh"
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PERMIT_URLS).permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(new JwtFilter(JWT_SECRET_KEY), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Value("${jwt.secret}")
    public void setKey(String key){
        JWT_SECRET_KEY = key;
    }



}

