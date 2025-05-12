package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.RoomCreateDto;
import com.giyeon.chat_server.dto.RoomInfoDto;
import com.giyeon.chat_server.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final RoomService roomService;

    @GetMapping("/api/me/rooms")
    public ApiResponseDto<?> getMyRooms(@PageableDefault(size = 50, page = 0) Pageable pageable) {

        List<RoomInfoDto> userRooms = roomService.getUserRooms(pageable);

        return ApiResponseDto.builder()
                .code(200)
                .data(userRooms)
                .build();
    }

    @PostMapping("/api/users/rooms")
    public ApiResponseDto<?> createRoom(@RequestBody RoomCreateDto roomCreateDto) {
        roomService.createRoom(roomCreateDto);
        return ApiResponseDto.builder()
                .code(200)
                .data("successfully created the room")
                .build();
    }


}
