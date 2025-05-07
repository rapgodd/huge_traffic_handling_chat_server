package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.AuthTokenDto;
import com.giyeon.chat_server.dto.LoginRequestDto;
import com.giyeon.chat_server.dto.SignupDto;
import com.giyeon.chat_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ApiResponseDto<?> login(@RequestBody LoginRequestDto loginDto) {

        AuthTokenDto authTokenDto = authService.authenticateUser(loginDto);

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }

    @PostMapping("/api/auth/refresh")
    public ApiResponseDto<?> refresh(@RequestBody AuthTokenDto refreshToken) {

        AuthTokenDto authTokenDto = authService.reset(refreshToken.getRefreshToken());

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }

    @PostMapping("/api/auth/signup")
    public ApiResponseDto<?> signup(@RequestBody SignupDto signupDto) {

        AuthTokenDto authTokenDto = authService.signup(signupDto);

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }

}
