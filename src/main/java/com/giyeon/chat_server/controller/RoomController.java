package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/api/room/{roomId}/exit")
    public ApiResponseDto<?> leaveRoom(@PathVariable Long roomId) {
        roomService.leaveRoom(roomId);
        return ApiResponseDto.builder()
                .code(200)
                .data("successfully leaved the room")
                .build();
    }

}
