package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;


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
    private ZonedDateTime leavedAt;
    private Long lastReadMessageId;
    private Boolean isJoined;


    public void updateToNow(ZonedDateTime now) {
        this.leavedAt = now;
    }

    public void updateToNull(){
        this.leavedAt = null;
    }

    @Builder
    public UserChatRoom(Long id, User user, ChatRoom chatRoom, ZonedDateTime leavedAt, Long lastReadMessageId, Boolean isJoined) {
        this.id = id;
        this.isJoined = isJoined;
        this.user = user;
        this.chatRoom = chatRoom;
        this.leavedAt = leavedAt;
        this.lastReadMessageId = lastReadMessageId;
    }

    public void updateNewMsgId(Long roomLastMsgId) {
        this.lastReadMessageId = roomLastMsgId;
    }

    public void updateIsJoined(Boolean isJoined) {
        this.isJoined = isJoined;
    }

    public void updateLeavedAt(ZonedDateTime now) {
        this.leavedAt = now;
    }
}
