package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class RoomDetailsDto {
    private Long roomId;
    private String roomName;
    private List<String> roomImageUrl;
    private String notification;
    private int joinedUserCount;


    @Builder
    public RoomDetailsDto(Long roomId, String roomName, List<String> roomImageUrl, String notification, int joinedUserCount) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomImageUrl = roomImageUrl;
        this.notification = notification;
        this.joinedUserCount = joinedUserCount;
    }
}
