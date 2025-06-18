package com.giyeon.chat_server.entity.message;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Table(name = "message")
@Entity
@ToString
@Getter
@NoArgsConstructor
public class Message {

    @Id
    private Long id;

    private String message;
    private String imageUrl;
    private Long roomId;
    private Long senderId;
    private ZonedDateTime createdAt;

    @Builder
    public Message(Long id,String message,Long roomId, Long senderId, ZonedDateTime createdAt, String imageUrl) {
        this.id = id;
        this.message = message;
        this.roomId = roomId;
        this.senderId = senderId;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }
}
