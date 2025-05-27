package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    @Query("SELECT uc FROM UserChatRoom uc" +
            " JOIN FETCH uc.user u" +
            " JOIN FETCH uc.chatRoom cr" +
            " WHERE u.id =:userId" +
            " AND uc.leavedAt IS NULL" +
            " ORDER BY cr.lastMessageId DESC")
    Page<UserChatRoom> findByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    //@Query를 사용해서 상위 5개의 방을 가져오는 쿼리
    @EntityGraph(attributePaths = "user")
    List<UserChatRoom> findByChatRoomOrderByIdAsc(ChatRoom room, Pageable pageable);

    Long countByChatRoom(ChatRoom room);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user u JOIN FETCH uc.chatRoom cr WHERE u.id =:userId AND cr.id =:roomId")
    UserChatRoom findUserInRoom(Long userId, Long roomId);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user u JOIN FETCH uc.chatRoom r WHERE r.id= :roomId AND u.id IN :localSessionUserIdList")
    List<UserChatRoom> findJoinedUsersInCurrentRoom(List<Long> localSessionUserIdList, Long roomId);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user JOIN FETCH uc.chatRoom WHERE uc.chatRoom.id=:roomId AND uc.leavedAt IS null")
    List<UserChatRoom> findAllUserChatRoomInSingleRoom(Long roomId);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user u JOIN FETCH uc.chatRoom cr WHERE u.id =:userId AND cr.id =:roomId")
    UserChatRoom findUserChatRoomByUserIdAndRoomId(Long userId, Long roomId);

    @Query("SELECT uc FROM UserChatRoom uc JOIN FETCH uc.user u JOIN FETCH uc.chatRoom cr WHERE cr.id =:roomId AND uc.isJoined = true")
    List<UserChatRoom> findJoinedUsers(Long roomId);
}
