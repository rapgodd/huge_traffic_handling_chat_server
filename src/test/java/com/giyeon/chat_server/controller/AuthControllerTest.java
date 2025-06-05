package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.AuthTokenDto;
import com.giyeon.chat_server.dto.SignupDto;
import com.giyeon.chat_server.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.BDDMockito.given;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    public void login_test() throws Exception {
        // given
        AuthTokenDto authTokenDto = AuthTokenDto.builder()
                .grantType("Bearer")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .userId(1L)
                .build();

        given(authService.authenticateUser(any())).willReturn(authTokenDto);

        //when
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content("{\"email\":\"user1@example.com\",\"password\":\"1234asd\"}")
                .with(csrf()))



        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(authTokenDto.getAccessToken()))
                .andExpect(jsonPath("$.data.refreshToken").value(authTokenDto.getRefreshToken()));
    }

    @Test
    public void signup_test() throws Exception {
        // given
        AuthTokenDto authTokenDto = AuthTokenDto.builder()
                .userId(1L)
                .build();
        given(authService.signup(any())).willReturn(authTokenDto);

        // when
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content("{\"name\":\"user1\",\"email\":\"user1@example.com\",\"password\":\"1234asd1234\"}")
                        .with(csrf()))

        // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(authTokenDto.getUserId()));
    }

    @Test
    public void refresh_test() throws Exception {

        // given
        AuthTokenDto refreshedToken = AuthTokenDto.builder()
                .grantType("Bearer")
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .userId(1L)
                .build();
        given(authService.reset(any())).willReturn(refreshedToken);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content("{\"refreshToken\":\"refreshToken\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value(refreshedToken.getAccessToken()))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshedToken.getRefreshToken()));


    }
}