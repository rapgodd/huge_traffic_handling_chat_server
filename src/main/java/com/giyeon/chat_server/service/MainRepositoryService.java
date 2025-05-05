package com.giyeon.chat_server.service;

import com.giyeon.chat_server.dto.RoomUsersDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.repository.main.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MainRepositoryService {
    
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public RoomUsersDto getRoom(Long roomId){
        return new RoomUsersDto(roomRepository.findUsersInRoom(roomId).getUserChatRooms());
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getReadOnly() {
        return roomRepository.findAll();
    }

    @Transactional
    public List<ChatRoom> getReadOnlyFalse() {
        return roomRepository.findAll();
    }
}
