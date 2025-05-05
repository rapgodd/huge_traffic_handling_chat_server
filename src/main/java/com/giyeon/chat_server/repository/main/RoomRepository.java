package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<ChatRoom,Long> {

    @Query("select r from ChatRoom r join fetch r.userChatRooms uc join fetch uc.user where r.id = :roomId")
    ChatRoom findUsersInRoom(@Param("roomId") Long roomId);

}
