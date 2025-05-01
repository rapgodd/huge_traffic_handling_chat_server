package com.giyeon.chat_server.service;

import com.giyeon.chat_server.entity.main.Room;
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
    public List<Room> getReadOnly() {
        return roomRepository.findAll();
    }

    @Transactional
    public List<Room> getReadOnlyFalse() {
        return roomRepository.findAll();
    }
}
