package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.*;
import com.giyeon.chat_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto loginDto) {

        AuthTokenDto authTokenDto = authService.authenticateUser(loginDto);

        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .code(200)
                        .data(authTokenDto)
                        .build()
                );
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@RequestBody AuthTokenDto refreshToken) {

        AuthTokenDto authTokenDto = authService.reset(refreshToken.getRefreshToken());

        return ResponseEntity.status(201)
                .body(ApiResponseDto.builder()
                        .code(201)
                        .data(authTokenDto)
                        .build()
                );
    }

    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupDto signupDto) {

        AuthTokenDto authTokenDto = authService.signup(signupDto);

        return ResponseEntity.status(201)
                .body(ApiResponseDto.builder()
                        .code(201)
                        .data(authTokenDto)
                        .build()
                );
    }

}
