package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .data("ok")
                        .code(200)
                        .build());
    }

}

