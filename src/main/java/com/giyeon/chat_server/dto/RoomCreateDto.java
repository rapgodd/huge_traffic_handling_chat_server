package com.giyeon.chat_server.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomCreateDto {

    private List<Long> otherUserIds;
    private String roomName;
    private String roomImageUrl;

}
