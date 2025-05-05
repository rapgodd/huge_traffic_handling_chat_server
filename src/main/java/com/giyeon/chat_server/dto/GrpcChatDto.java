package com.giyeon.chat_server.dto;

import lombok.Data;

@Data
public class GrpcChatDto {

    private String message;
    private Long sender;

}
