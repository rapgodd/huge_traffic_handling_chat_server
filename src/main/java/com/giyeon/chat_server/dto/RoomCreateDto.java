package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class RoomCreateDto {

    private List<Long> otherUserIds;
    private String roomName;
    private String roomImageUrl;

    @Builder
    public RoomCreateDto(List<Long> otherUserIds, String roomName, String roomImageUrl) {
        this.otherUserIds = otherUserIds;
        this.roomName = roomName;
        this.roomImageUrl = roomImageUrl;
    }

}
