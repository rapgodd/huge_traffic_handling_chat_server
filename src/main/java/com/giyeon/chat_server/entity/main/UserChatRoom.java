package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;


@Entity
@Table(name = "user_chat_rooms")
@Getter
@ToString
public class UserChatRoom {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

}
