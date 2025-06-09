package com.giyeon.chat_server.service.redisService.repositoryService;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainRepositoryService {
    
    private final RoomRepository roomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    @Transactional(readOnly = false)
    public RoomUsersDto getRoom(Long roomId){
        return new RoomUsersDto(roomRepository.findUsersInRoom(roomId).getUserChatRooms());
    }
    @Transactional(readOnly = false)
    public UserChatRoom findUserChatRoom(Long userChatRoomId) {
        UserChatRoom userChatRoom = userChatRoomRepository.findById(userChatRoomId).get();
        return userChatRoom;
    }

    @Transactional(readOnly = false)
    public List<UserChatRoom> getUserChatRooms(Long userId, Pageable pageable){
        return userChatRoomRepository.findByUser(userId, pageable).getContent();
    }

    @Transactional(readOnly = false)
    public List<ChatRoom> getRoomList(ArrayList<Long> roomIds) {
        return roomRepository.findAllUserChatRooms(roomIds);
    }

    @Transactional(readOnly = false)
    public void removeJoinedUserInRoom(Long roomId, Long userId) {
        UserChatRoom userInRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userInRoom.updateToNow(ZonedDateTime.now());
        userInRoom.updateIsJoined(false);
    }


    @Transactional(readOnly = false)
    public void setUserChatRoomToClose(Long roomId, Long userId) {
        UserChatRoom userInRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userInRoom.updateIsJoined(false);
    }

    @Transactional(readOnly = false)
    public List<Long> findJoinedUsersInCurrentRoom(List<Long> localSessionUserIdList, Long roomId) {
        List<UserChatRoom> joinedUsersInCurrentRoom = userChatRoomRepository.findJoinedUsersInCurrentRoom(localSessionUserIdList, roomId);
        return joinedUsersInCurrentRoom.stream().map(uc -> {
            return uc.getUser().getId();
        }).toList();

    }

    @Transactional(readOnly = false)
    public ChatRoom findRoom(Long roomId) {
        ChatRoom usersInRoom = roomRepository.findUsersInRoom(roomId);
        return usersInRoom;
    }

    @Transactional(readOnly = false)
    public void saveUserChatRooms(ChatRoom chatRoom, List<UserChatRoom> userChatRooms) {
        roomRepository.save(chatRoom);
        userChatRoomRepository.saveAll(userChatRooms);
    }

    @Transactional(readOnly = false)
    public List<UserChatRoom> findUserChatRoomList(Long roomId) {
        return userChatRoomRepository.findAllUserChatRoomInSingleRoom(roomId);
    }


    @Transactional(readOnly = false)
    public void updateUserChatRoomNewMsgId(Long roomId, Long userId, Long roomLastMsgId) {
        UserChatRoom room = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(userId, roomId);
        room.updateNewMsgId(roomLastMsgId);
    }

    @Transactional(readOnly = false)
    public Long getLastMsgIdInRoom(Long roomId) {
        ChatRoom chatRoom = roomRepository.findById(roomId).orElseThrow();
        return chatRoom.getLastMessageId();
    }

    @Transactional(readOnly = false)
    public Long getLastMsgIdInUserChatRoom(Long userId, Long roomId) {
        UserChatRoom userChatRoom = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(userId, roomId);
        return userChatRoom.getLastReadMessageId();
    }

    @Transactional(readOnly = false)
    public List<Long> getJoinedUserChatRooms(Long roomId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findJoinedUsers(roomId);
        List<Long> list = userChatRooms.stream().map(uc -> {
            return uc.getUser().getId();
        }).toList();
        return list;
    }

    @Transactional(readOnly = false)
    public void updateUserChatRoomToJoined(Long roomId, Long userId) {
        UserChatRoom userChatRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userChatRoom.updateIsJoined(true);
    }

    @Transactional(readOnly = false)
    public void updateRoomLastMsgId(Long roomId, Long lastMsgId) {
        ChatRoom chatRoom = roomRepository.findById(roomId).orElseThrow();
        chatRoom.updateLastMessageId(lastMsgId);
    }

    @Transactional(readOnly = false)
    public void batchUpdateUserChatRoomsNewMsgId(Long chatRoomId, List<Long> userIds, Long aLong) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findJoinedUsersInCurrentRoom(userIds, chatRoomId);
        for (UserChatRoom userChatRoom : userChatRooms) {
            userChatRoom.updateNewMsgId(aLong);
        }
    }
}
