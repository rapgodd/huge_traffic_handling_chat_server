package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    // ChatRoom엔티티의 last_message_time 필드로 50개 최신 방을 가져오는 쿼리
    @EntityGraph(attributePaths = "chatRoom")
    Page<UserChatRoom> findByUserOrderByChatRoom_LastMessageTimeDesc(
            User user,
            Pageable pageable
    );

    //@Query를 사용해서 상위 5개의 방을 가져오는 쿼리
    @EntityGraph(attributePaths = "user")
    List<UserChatRoom> findByChatRoomOrderByIdAsc(ChatRoom room, Pageable pageable);

    Long countByChatRoom(ChatRoom room);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user u JOIN FETCH uc.chatRoom cr WHERE u.id =:userId AND cr.id =:roomId")
    UserChatRoom findUserInRoom(Long userId, Long roomId);
}
