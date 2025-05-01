package com.giyeon.chat_server.dto;

import lombok.Data;

@Data
public class MessageDto {
    private String message;
    private String sender;
}
