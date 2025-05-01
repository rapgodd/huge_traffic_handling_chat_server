package com.giyeon.chat_server.service;

import com.giyeon.chat_server.repository.main.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MainService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public String getRoom(){
        return roomRepository.findById(1L).toString();
    }

}
