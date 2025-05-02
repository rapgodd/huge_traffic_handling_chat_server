package com.giyeon.chat_server.entity.main;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@ToString
public class ChatRoom {

    @Id
    private Long id;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom")
    private List<UserChatRoom> userChatRooms;

}
