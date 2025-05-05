package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomMessagesDto {
    private String message;
    private String sender;
}
