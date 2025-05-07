package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.AuthTokenDto;
import com.giyeon.chat_server.dto.LoginRequestDto;
import com.giyeon.chat_server.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;

    @PostMapping("/api/login")
    public ApiResponseDto<?> login(@RequestBody LoginRequestDto loginDto) {

        AuthTokenDto authTokenDto = loginService.authenticateUser(loginDto);

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }

    @PostMapping("/api/refresh")
    public ApiResponseDto<?> refresh(@RequestBody AuthTokenDto refreshToken) {

        AuthTokenDto authTokenDto = loginService.reset(refreshToken.getRefreshToken());

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }


}
