package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.service.MessageRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepositoryService messageRepositoryService;

    @PostMapping("/")
    public String insertMessage(@RequestBody Long id){
        return messageRepositoryService.insertMessage(id).toString();
    }

    @GetMapping("/message")
    public ApiResponseDto<?> getMessages(@RequestParam(name = "room") Long roomId){
        return ApiResponseDto.builder()
                .code(200)
                .data(messageRepositoryService.getMessages(roomId))
                .build();
    }

}
