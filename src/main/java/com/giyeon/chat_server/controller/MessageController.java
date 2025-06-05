package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/api/messages")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid ChatDto chatDto) {
        messageService.sendMessage(chatDto);

        return ResponseEntity
                .status(204)
                .build();
    }


}
