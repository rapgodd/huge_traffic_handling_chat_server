package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomMessageListDto {
    private String senderName;
    private String senderImageUrl;
    private String message;
    private LocalDateTime createdAt;
    private int unreadCount;
    private boolean isMe;

    @Builder
    public RoomMessageListDto(String senderName, String senderImageUrl, String message, LocalDateTime createdAt, int unreadCount, boolean isMe) {
        this.senderName = senderName;
        this.senderImageUrl = senderImageUrl;
        this.message = message;
        this.createdAt = createdAt;
        this.unreadCount = unreadCount;
        this.isMe = isMe;
    }
}
