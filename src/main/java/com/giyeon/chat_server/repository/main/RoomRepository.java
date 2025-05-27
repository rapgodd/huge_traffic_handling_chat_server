package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<ChatRoom,Long> {

    @Query("select r from ChatRoom r join fetch r.userChatRooms uc join fetch uc.user where r.id = :roomId")
    ChatRoom findUsersInRoom(@Param("roomId") Long roomId);

    @Query("select r from ChatRoom r join fetch r.userChatRooms uc join fetch uc.user where r.roomName = :roomName")
    ChatRoom findByRoomName(@Param("roomName") String roomName);


    @Query("select r from ChatRoom r join fetch r.userChatRooms uc join fetch uc.user where r.id in :roomIds")
    List<ChatRoom> findAllUserChatRooms(ArrayList<Long> roomIds);
}
