package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.service.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepositoryService messageRepositoryService;
    private final MessageService messageService;

    @PostMapping("/api/messages")
    public ApiResponseDto<?> sendMessage(@RequestBody ChatDto chatDto) {
        messageService.sendMessage(chatDto);
        return ApiResponseDto.builder()
                .code(200)
                .data("ok")
                .build();
    }


}
