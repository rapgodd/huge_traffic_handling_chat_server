package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.RoomDetailsDto;
import com.giyeon.chat_server.dto.RoomMessageListDto;
import com.giyeon.chat_server.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @DeleteMapping("/api/rooms/{roomId}/exit")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId) {
        roomService.leaveRoom(roomId);

        return ResponseEntity
                .status(204)
                .build();
    }

    @PostMapping("/api/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId) {
        roomService.joinRoom(roomId);
        return ResponseEntity
                .status(204)
                .build();
    }

    @GetMapping("/api/rooms/{roomId}")
    public ResponseEntity<?> getRoomDetails(@PathVariable Long roomId) {
        RoomDetailsDto roomDetailsDto = roomService.getRoomDetails(roomId);

        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .code(200)
                        .data(roomDetailsDto)
                        .build());
    }

    @GetMapping("/api/rooms/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(@PageableDefault(size = 50, page = 0) Pageable pageable,
                                                 @PathVariable Long roomId) {
        List<RoomMessageListDto> messages = roomService.getRoomMessages(roomId, pageable);

        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .code(200)
                        .data(messages)
                        .build());

    }

    @PutMapping("/api/rooms/{roomId}/close")
    public ResponseEntity<?> closeRoom(@PathVariable Long roomId) {
        roomService.closeRoom(roomId);

        return ResponseEntity
                .status(204)
                .build();
    }


}
