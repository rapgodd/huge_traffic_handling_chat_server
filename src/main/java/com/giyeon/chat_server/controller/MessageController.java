package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/")
    public String insertMessage(@RequestBody Long id){
        return messageService.insertMessage(id).toString();
    }



}
