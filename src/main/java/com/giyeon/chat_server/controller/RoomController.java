package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final MainService mainService;

    @GetMapping("/room")
    public String getRoom() {
        return mainService.getRoom();
    }

}
