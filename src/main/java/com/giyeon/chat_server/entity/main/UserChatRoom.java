package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_chat_rooms")
@Getter
@NoArgsConstructor
public class UserChatRoom {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;
    private LocalDateTime leavedAt;


    public void leaveRoom(LocalDateTime now) {
        this.leavedAt = now;
    }
}
