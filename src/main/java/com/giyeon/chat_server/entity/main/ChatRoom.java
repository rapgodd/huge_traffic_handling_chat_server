package com.giyeon.chat_server.entity.main;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Id
    private Long id;

    private ZonedDateTime createdAt;
    private String roomName;
    private String roomImageUrl;
    private String notification;
    private Long lastMessageId;

    @OneToMany(mappedBy = "chatRoom")
    private List<UserChatRoom> userChatRooms;

    @Builder
    public ChatRoom(Long id, ZonedDateTime createdAt, String roomName, String roomImageUrl, String notification, Long lastMessageId) {
        this.id = id;
        this.createdAt = createdAt;
        this.roomName = roomName;
        this.roomImageUrl = roomImageUrl;
        this.notification = notification;
        this.lastMessageId = lastMessageId;
    }

    public void updateRoomNameAndImageUrl(String roomName, String roomImagesUrl) {
        this.roomName = roomName;
        this.roomImageUrl = roomImagesUrl;
    }
}
