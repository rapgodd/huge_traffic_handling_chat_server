package com.giyeon.chat_server.service;

import com.giyeon.chat_server.dto.RoomInfoDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.service.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    private final Environment environment;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;



    public List<RoomInfoDto> getUserRooms(Pageable pageable){
        //userId --> user object
        Long userId = JwtUtil.getUserId(environment.getProperty("jwt.secret"));
        User user = User.builder().id(userId).build();

        //user.getUserChatRooms() --> roomId
        List<UserChatRoom> userChatRooms = mainRepositoryService.getUserChatRooms(user,pageable);

        //userChatRooms -Loop-> roomId,roomName,roomImageUrl,leavedAt
        List<RoomInfoDto> roomInfoDtos = userChatRooms.stream()
                .map(
                        userChatRoom -> {

                            ChatRoom chatRoom = userChatRoom.getChatRoom();
                            String roomName = redisTemplate.opsForValue().get("chatRoomId:" + chatRoom.getId() + ":roomName");
                            String roomImageUrl = redisTemplate.opsForValue().get("chatRoomId:" + chatRoom.getId() + ":roomImageUrl");
                            //Room에 방이름과 방 이미지 다 없을때
                            // --> 동적으로 방 참여 유저 정보를 바탕으로 생성
                            if(roomName==null && roomImageUrl ==null){

                                // 방 --> 유저 방
                                List<UserChatRoom> userChatRoomList = mainRepositoryService.find5UserChatRoomsInRoom(chatRoom);

                                // 유저 방 --> 유저 --> 유저 이름 , 유저 이미지 가져오기
                                StringBuilder sb = new StringBuilder();
                                ArrayList<String> roomImages = new ArrayList<>();
                                int flag=0;
                                for (UserChatRoom room : userChatRoomList) {
                                    String name = room.getUser().getName();
                                    sb.append(name+" ");
                                    if(flag<=1){
                                        if (room.getUser().getUserImageUrl() != null) {
                                            roomImages.add(room.getUser().getUserImageUrl());
                                            flag += 1;
                                        }
                                    }
                                }
                                String resultRoomName = sb.toString();

                                // 총 방 참여 인원 수
                                int count = Integer.parseInt(String.valueOf(mainRepositoryService.countAllUsersInRoom(chatRoom)));


                                // 마지막 참여 시점
                                LocalDateTime leavedAt = userChatRoom.getLeavedAt();
                                // 안읽은 메세지
                                List<Message> unreadMessages = messageRepositoryService.getMessagesGreaterThan(chatRoom.getId(), leavedAt);
                                // 안 읽은 메세지 수
                                int unreadCount = unreadMessages.size();
                                // 마지막 메세지
                                String lastMessage = unreadMessages.get(unreadMessages.size()-1).getMessage();

                                redisTemplate.opsForValue().set("chatRoomId:" + chatRoom.getId() + ":roomName", resultRoomName,30, TimeUnit.MINUTES);
                                redisTemplate.opsForValue().set("chatRoomId:" + chatRoom.getId() + ":roomImageUrl", String.join(", ", roomImages),30, TimeUnit.MINUTES);



                                return RoomInfoDto.builder()
                                        .roomId(chatRoom.getId())
                                        .roomImageUrl(roomImages)
                                        .roomName(resultRoomName)
                                        .joinedUserCount(count)
                                        .unreadCount(unreadCount)
                                        .lastMessageTime(chatRoom.getLastMessageTime())
                                        .lastMessage(lastMessage)
                                        .build();
                                // 방이름만 없고 이미지는 있는 경우
                            }else if (roomName==null&& roomImageUrl!=null){
                                // joined with user
                                List<UserChatRoom> userChatRoomList = mainRepositoryService.find5UserChatRoomsInRoom(chatRoom);

                                // roomName
                                String resultRoomName = getRoomName(userChatRoomList);

                                //총 방 참여 인원 수
                                int count = Integer.parseInt(String.valueOf(mainRepositoryService.countAllUsersInRoom(chatRoom)));

                                // 마지막 참여 시점
                                LocalDateTime leavedAt = userChatRoom.getLeavedAt();

                                // 안읽은 메세지
                                List<Message> unreadMessages = messageRepositoryService.getMessagesGreaterThan(chatRoom.getId(), leavedAt);

                                // 안 읽은 메세지 수
                                int unreadCount = unreadMessages.size();

                                // 마지막 메세지
                                String lastMessage = unreadMessages.get(unreadMessages.size()-1).getMessage();

                                redisTemplate.opsForValue().set("chatRoomId:" + chatRoom.getId() + ":roomName", resultRoomName,30, TimeUnit.MINUTES);


                                return RoomInfoDto.builder()
                                        .roomId(chatRoom.getId())
                                        .roomImageUrl(List.of(roomImageUrl.split(", ")))
                                        .roomName(resultRoomName)
                                        .joinedUserCount(count)
                                        .unreadCount(unreadCount)
                                        .lastMessageTime(chatRoom.getLastMessageTime())
                                        .lastMessage(lastMessage)
                                        .build();

                                // 방이름만 있는경우
                            }else if(roomName!=null&& roomImageUrl==null){
                                // joined with user
                                List<UserChatRoom> userChatRoomList = mainRepositoryService.find5UserChatRoomsInRoom(chatRoom);

                                // roomImgUrl
                                ArrayList<String> roomImages = getRoomImages(userChatRoomList);

                                int count = Integer.parseInt(String.valueOf(mainRepositoryService.countAllUsersInRoom(chatRoom)));

                                redisTemplate.opsForValue().set("chatRoomId:" + chatRoom.getId() + ":roomImageUrl", String.join(", ", roomImages),30, TimeUnit.MINUTES);

                                // message
                                LocalDateTime leavedAt = userChatRoom.getLeavedAt();
                                List<Message> unreadMessages = messageRepositoryService.getMessagesGreaterThan(chatRoom.getId(), leavedAt);
                                int unreadCount = unreadMessages.size();
                                String lastMessage = unreadMessages.get(unreadMessages.size()-1).getMessage();




                                return RoomInfoDto.builder()
                                        .roomId(chatRoom.getId())
                                        .roomImageUrl(roomImages)
                                        .roomName(roomName)
                                        .joinedUserCount(count)
                                        .unreadCount(unreadCount)
                                        .lastMessageTime(chatRoom.getLastMessageTime())
                                        .lastMessage(lastMessage)
                                        .build();
                            }else{
                                int count = Integer.parseInt(String.valueOf(mainRepositoryService.countAllUsersInRoom(chatRoom)));
                                LocalDateTime leavedAt = userChatRoom.getLeavedAt();
                                List<Message> unreadMessages = messageRepositoryService.getMessagesGreaterThan(chatRoom.getId(), leavedAt);
                                int unreadCount = unreadMessages.size();
                                String lastMessage = unreadMessages.get(unreadMessages.size()-1).getMessage();



                                return RoomInfoDto.builder()
                                        .roomId(chatRoom.getId())
                                        .roomImageUrl(List.of(roomImageUrl.split(", ")))
                                        .roomName(roomName)
                                        .joinedUserCount(count)
                                        .unreadCount(unreadCount)
                                        .lastMessageTime(chatRoom.getLastMessageTime())
                                        .lastMessage(lastMessage)
                                        .build();
                            }
                        }).toList();

        return roomInfoDtos;
    }

    private static ArrayList<String> getRoomImages(List<UserChatRoom> userChatRoomList) {
        ArrayList<String> roomImages = new ArrayList<>();
        int flag=0;
        for (UserChatRoom room : userChatRoomList) {
            if(flag<=1){
                if (room.getUser().getUserImageUrl() != null) {
                    roomImages.add(room.getUser().getUserImageUrl());
                    flag += 1;
                }
            }else{
                break;
            }
        }
        return roomImages;
    }

    private String getRoomName(List<UserChatRoom> userChatRoomList) {
        StringBuilder sb = new StringBuilder();
        for (UserChatRoom room : userChatRoomList) {
            String name = room.getUser().getName();
            sb.append(name+" ");
        }
        String roomName = sb.toString();
        return roomName;
    }

}
