package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.service.repositoryService.MainRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class RoomController {

    private final MainRepositoryService mainRepositoryService;

    @GetMapping("/tx/readonly")
    public ApiResponseDto<?> getReadOnly() {
        List<ChatRoom> readOnly = mainRepositoryService.getReadOnly();
        return ApiResponseDto.builder()
                .code(200)
                .data(readOnly)
                .build();
    }

    @GetMapping("/tx")
    public ApiResponseDto<?> getReadOnlyFalse() {
        List<ChatRoom> readOnlyFalse = mainRepositoryService.getReadOnlyFalse();
        return ApiResponseDto.builder()
                .code(200)
                .data(readOnlyFalse)
                .build();
    }

}
