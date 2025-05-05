package com.giyeon.chat_server.dto;

import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class RoomUsersDto {

    private List<UserChatRoom> userChatRoomList;

    public RoomUsersDto(List<UserChatRoom> userChatRoomList) {
        this.userChatRoomList = userChatRoomList;
    }
}
