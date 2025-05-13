package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.*;

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


    public void updateToNow(LocalDateTime now) {
        this.leavedAt = now;
    }

    public void updateToNull(){
        this.leavedAt = null;
    }

    @Builder
    public UserChatRoom(Long id, User user, ChatRoom chatRoom, LocalDateTime leavedAt) {
        this.id = id;
        this.user = user;
        this.chatRoom = chatRoom;
        this.leavedAt = leavedAt;
    }
}
