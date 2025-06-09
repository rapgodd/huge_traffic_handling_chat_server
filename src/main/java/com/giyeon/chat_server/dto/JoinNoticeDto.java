package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class JoinNoticeDto {
    private Long userId;
    private Long lastMessageId;
    private String message;

    @Builder
    public JoinNoticeDto(Long userId, Long lastMessageId) {
        this.userId = userId;
        this.lastMessageId = lastMessageId;
        this.message = userId + "님이 입장하셨습니다.";
    }

}
