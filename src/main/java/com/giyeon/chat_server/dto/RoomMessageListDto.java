package com.giyeon.chat_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
public class RoomMessageListDto {
    private String senderName;
    private String senderImageUrl;
    private String messageImgUrl;
    private String message;
    private ZonedDateTime createdAt;
    private int unreadCount;
    @JsonProperty("isMe")
    private boolean isMe;

    @Builder
    public RoomMessageListDto(String senderName, String senderImageUrl, String message, ZonedDateTime createdAt, int unreadCount, boolean isMe, String msgImgUrl) {
        this.senderName = senderName;
        this.senderImageUrl = senderImageUrl;
        this.message = message;
        this.createdAt = createdAt;
        this.unreadCount = unreadCount;
        this.isMe = isMe;
        this.messageImgUrl = msgImgUrl;
    }
}
