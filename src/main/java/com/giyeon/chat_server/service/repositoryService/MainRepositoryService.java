package com.giyeon.chat_server.service.repositoryService;

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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainRepositoryService {
    
    private final RoomRepository roomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    @Transactional(readOnly = true)
    public RoomUsersDto getRoom(Long roomId){
        return new RoomUsersDto(roomRepository.findUsersInRoom(roomId).getUserChatRooms());
    }

    @Transactional
    public List<UserChatRoom> getUserChatRooms(User user, Pageable pageable){
        return userChatRoomRepository.findByUserOrderByChatRoom_LastMessageTimeDesc(user, pageable).getContent();
    }

    public void saveAllUserChatRoom(List<UserChatRoom> userChatRooms) {
        userChatRoomRepository.saveAll(userChatRooms);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomList(ArrayList<Long> roomIds) {
        return roomRepository.findAllUserChatRooms(roomIds);
    }

    @Transactional(readOnly = false)
    public void updateLeavedAt(Long roomId, Long userId) {
        UserChatRoom userInRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userInRoom.leaveRoom(LocalDateTime.now());
    }
}
