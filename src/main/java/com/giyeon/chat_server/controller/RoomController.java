package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.RoomDetailsDto;
import com.giyeon.chat_server.dto.RoomMessageListDto;
import com.giyeon.chat_server.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/api/rooms/{roomId}/exit")
    public ApiResponseDto<?> leaveRoom(@PathVariable Long roomId) {
        roomService.leaveRoom(roomId);
        return ApiResponseDto.builder()
                .code(200)
                .data("successfully leaved the room")
                .build();
    }

    @PostMapping("/api/rooms/{roomId}/join")
    public ApiResponseDto<?> joinRoom(@PathVariable Long roomId) {
        roomService.joinRoom(roomId);
        return ApiResponseDto.builder()
                .code(200)
                .data("successfully joined the room")
                .build();
    }

    @GetMapping("/api/rooms/{roomId}")
    public ApiResponseDto<?> getRoomDetails(@PathVariable Long roomId) {
        RoomDetailsDto roomDetailsDto = roomService.getRoomDetails(roomId);
        return ApiResponseDto.builder()
                .code(200)
                .data(roomDetailsDto)
                .build();
    }

    @GetMapping("/api/rooms/{roomId}/messages")
    public ApiResponseDto<?> getRoomMessages(@PageableDefault(size = 50, page = 0) Pageable pageable,
                                                 @PathVariable Long roomId) {
        List<RoomMessageListDto> messages = roomService.getRoomMessages(roomId, pageable);
        return ApiResponseDto.builder()
                .code(200)
                .data(messages)
                .build();
    }


}
