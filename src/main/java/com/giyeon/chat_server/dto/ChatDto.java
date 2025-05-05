package com.giyeon.chat_server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatDto {

    private String message;
    private Long roomId;
    private LocalDateTime createdAt;
    private Long senderId;
    private String senderName;
    private Boolean reply;
    private Long replyingTo;

}
