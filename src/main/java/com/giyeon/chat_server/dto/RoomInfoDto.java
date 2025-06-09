package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class RoomInfoDto {

    private String roomName;
    private List<String> roomImageUrl;
    private Long roomId;
    private int unreadCount;
    private int joinedUserCount;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;

    @Builder
    public RoomInfoDto(String roomName, List<String> roomImageUrl, Long roomId, int unreadCount, int joinedUserCount, String lastMessage, ZonedDateTime lastMessageTime) {
        this.roomName = roomName;
        this.roomImageUrl = roomImageUrl;
        this.roomId = roomId;
        this.unreadCount = unreadCount;
        this.joinedUserCount = joinedUserCount;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
}
