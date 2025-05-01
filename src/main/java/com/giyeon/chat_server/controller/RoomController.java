package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.entity.main.Room;
import com.giyeon.chat_server.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class RoomController {

    private final MainService mainService;

    @GetMapping("/room")
    public String getRoom() {
        return mainService.getRoom();
    }

    @GetMapping("/tx/readonly")
    public ApiResponseDto<?> getReadOnly() {
        List<Room> readOnly = mainService.getReadOnly();
        return ApiResponseDto.builder()
                .code(200)
                .data(readOnly)
                .build();
    }

    @GetMapping("/tx")
    public ApiResponseDto<?> getReadOnlyFalse() {
        List<Room> readOnlyFalse = mainService.getReadOnlyFalse();
        return ApiResponseDto.builder()
                .code(200)
                .data(readOnlyFalse)
                .build();
    }

}
