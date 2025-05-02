package com.giyeon.chat_server.service;

import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.repository.main.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {
    
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public String getRoom(){
        return roomRepository.findById(1L).toString();
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
