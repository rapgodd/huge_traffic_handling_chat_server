package com.giyeon.chat_server.service.repositoryService;

import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<UserChatRoom> find5UserChatRoomsInRoom(ChatRoom room) {
        PageRequest pageRequest = PageRequest.of(0, 5);
        return userChatRoomRepository.findByChatRoomOrderByIdAsc(room, pageRequest);
    }

    @Transactional(readOnly = true)
    public Long countAllUsersInRoom(ChatRoom room) {
        return userChatRoomRepository.countByChatRoom(room);
    }

    public void saveAllUserChatRoom(List<UserChatRoom> userChatRooms) {
        userChatRoomRepository.saveAll(userChatRooms);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomList(ArrayList<Long> roomIds) {
        return roomRepository.findAllUserChatRooms(roomIds);
    }
}
