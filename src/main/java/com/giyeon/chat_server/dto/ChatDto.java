package com.giyeon.chat_server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ChatDto {

    @NotBlank(message = "메시지는 필수 입력입니다.")
    private String message;
    private Long roomId;
    private ZonedDateTime createdAt;
    private Long senderId;
    private String senderName;
    private int joinedUser;

    @Builder
    public ChatDto(String message, Long roomId, ZonedDateTime createdAt, Long senderId, String senderName, int joinedUser){
        this.joinedUser = joinedUser;
        this.message = message;
        this.roomId = roomId;
        this.createdAt = createdAt;
        this.senderName = senderName;
        this.senderId = senderId;
    }
}
