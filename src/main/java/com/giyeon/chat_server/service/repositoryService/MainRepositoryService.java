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

    @Transactional(readOnly = false)
    public RoomUsersDto getRoom(Long roomId){
        return new RoomUsersDto(roomRepository.findUsersInRoom(roomId).getUserChatRooms());
    }

    @Transactional
    public List<UserChatRoom> getUserChatRooms(User user, Pageable pageable){
        return userChatRoomRepository.findByUser(user, pageable).getContent();
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomList(ArrayList<Long> roomIds) {
        return roomRepository.findAllUserChatRooms(roomIds);
    }

    @Transactional(readOnly = false)
    public void updateLeavedAtToNow(Long roomId, Long userId) {
        UserChatRoom userInRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userInRoom.updateToNow(LocalDateTime.now());
    }

    @Transactional(readOnly = false)
    public void updateLeavedAtToNull(Long roomId, Long userId) {
        UserChatRoom userChatRoom = userChatRoomRepository.findUserInRoom(userId, roomId);
        userChatRoom.updateToNull();
    }

    @Transactional(readOnly = false)
    public List<Long> findJoinedUsersInCurrentRoom(List<Long> localSessionUserIdList, Long roomId) {
        List<UserChatRoom> joinedUsersInCurrentRoom = userChatRoomRepository.findJoinedUsersInCurrentRoom(localSessionUserIdList, roomId);
        return joinedUsersInCurrentRoom.stream().map(uc -> {
            return uc.getUser().getId();
        }).toList();

    }

    @Transactional(readOnly = true)
    public ChatRoom findRoom(Long roomId) {
        ChatRoom usersInRoom = roomRepository.findUsersInRoom(roomId);
        return usersInRoom;
    }

    @Transactional(readOnly = false)
    public void saveUserChatRooms(List<UserChatRoom> userChatRooms) {
        userChatRoomRepository.saveAll(userChatRooms);
    }

    @Transactional(readOnly = false)
    public void saveRoom(ChatRoom chatRoom) {
        roomRepository.save(chatRoom);
    }
}
