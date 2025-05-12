package com.giyeon.chat_server.entity.main;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Setter
    @Id
    private Long id;

    private LocalDateTime createdAt;
    private String roomName;
    private String roomImageUrl;
    private String notification;

    @OneToMany(mappedBy = "chatRoom")
    private List<UserChatRoom> userChatRooms;

    @Builder
    public ChatRoom(Long id, LocalDateTime createdAt, String roomName, String roomImageUrl, String notification) {
        this.id = id;
        this.createdAt = createdAt;
        this.roomName = roomName;
        this.roomImageUrl = roomImageUrl;
        this.notification = notification;
    }

    public void updateRoomNameAndImageUrl(String roomName, String roomImagesUrl) {
        this.roomName = roomName;
        this.roomImageUrl = roomImagesUrl;
    }

}
