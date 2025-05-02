package com.giyeon.chat_server.entity.message;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table(name = "message")
@Entity
@ToString
@Getter
@Setter
public class Message {

    @Id
    private Long id;

    private String message;
    private Long roomId;

}
