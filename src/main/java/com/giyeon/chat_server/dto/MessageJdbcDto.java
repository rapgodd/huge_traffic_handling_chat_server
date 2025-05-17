package com.giyeon.chat_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@ToString
public class MessageJdbcDto {
    private Long roomId;
    private Integer unreadCount;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;
}