package com.giyeon.chat_server.service;

import com.giyeon.chat_server.dto.ImageUrlDto;
import com.giyeon.chat_server.properties.JwtProperty;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MainRepositoryService mainRepositoryService;
    private final JwtProperty jwtProperty;

    public void uploadProfileImg(ImageUrlDto urlDto){
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        mainRepositoryService.updateUserProfileImage(userId,urlDto.getImageUrl());
    }

}
