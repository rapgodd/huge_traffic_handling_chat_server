package com.giyeon.chat_server.entity.message;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "message")
@Entity
@ToString
@Getter
@NoArgsConstructor
public class Message {

    @Id
    private Long id;

    private String message;
    private Long roomId;
    private Long senderId;
    private LocalDateTime createdAt;

    @Builder
    public Message(Long id,String message,Long roomId){
        this.id = id;
        this.message = message;
        this.roomId = roomId;
    }
}
