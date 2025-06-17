package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ApiResponseDto;
import com.giyeon.chat_server.dto.RoomCreateDto;
import com.giyeon.chat_server.dto.RoomInfoDto;
import com.giyeon.chat_server.service.RoomService;
import com.giyeon.chat_server.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final RoomService roomService;
    private final S3Service s3Service;

    @GetMapping("/api/me/rooms")
    public ResponseEntity<?> getMyRooms(@PageableDefault(size = 50, page = 0) Pageable pageable) {

        List<RoomInfoDto> userRooms = roomService.getUserRooms(pageable);

        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .code(200)
                        .data(userRooms)
                        .build());
    }

    @PostMapping("/api/users/rooms")
    public ResponseEntity<?> createRoom(@RequestBody RoomCreateDto roomCreateDto) {
        roomService.createRoom(roomCreateDto);

        return ResponseEntity.status(204)
                .build();
    }

    @PostMapping("/api/users/image")
    public ResponseEntity<?> uploadImage(@RequestPart(value = "image", required = false) MultipartFile image){

        String url = s3Service.upload(image);

        return ResponseEntity.status(200)
                .body(ApiResponseDto.builder()
                        .code(200)
                        .data(url)
                        .build());

    }


}
