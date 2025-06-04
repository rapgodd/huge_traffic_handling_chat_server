package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.*;
import com.giyeon.chat_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ApiResponseDto<?> login(@RequestBody @Valid LoginRequestDto loginDto) {

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
    public ApiResponseDto<?> signup(@RequestBody @Valid SignupDto signupDto) {

        AuthTokenDto authTokenDto = authService.signup(signupDto);

        return ApiResponseDto.builder()
                .code(200)
                .data(authTokenDto)
                .build();
    }

}
