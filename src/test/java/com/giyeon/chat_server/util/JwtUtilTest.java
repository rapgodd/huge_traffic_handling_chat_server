package com.giyeon.chat_server.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class JwtUtilTest {

    @Autowired
    private Environment env;

    @Test
    void getUserIdTest(){

        //given
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODUzNTQ2ODU0NTA4MTc1MzYiLCJyb2xlIjpbXSwiaWF0IjoxNzQ4MjU5Mjc3LCJleHAiOjE3NDgyNjEwNzd9.NJmXwhc5-wBgvQgrTGDINyMTe-1adKcgWMWTZs-0kFzLrvUSKJLcuLtluQ6nE_kY3Zlx_8pbrbO2NSetON_NCw";
        MockedStatic<JwtUtil> jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        String secretKey = env.getProperty("jwt.secret");
        jwtUtilMockedStatic.when(JwtUtil::getAccessToken).thenReturn(accessToken);
        jwtUtilMockedStatic.when(() -> JwtUtil.getUserId(secretKey)).thenCallRealMethod();
        jwtUtilMockedStatic.when(()-> JwtUtil.parseClaims(accessToken,secretKey)).thenCallRealMethod();

        //when
        Long userId = JwtUtil.getUserId(secretKey);

        //then
        assertThat(userId).isEqualTo(185354685450817536L);
        jwtUtilMockedStatic.close();

    }



}