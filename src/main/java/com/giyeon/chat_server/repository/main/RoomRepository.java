package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.entity.main.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<ChatRoom,Long> {

}
