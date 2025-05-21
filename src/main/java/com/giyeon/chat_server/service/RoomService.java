package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.component.MsgKeySelector;
import com.giyeon.chat_server.dto.*;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.giyeon.chat_server.properties.JwtProperty;
import com.giyeon.chat_server.service.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    private final JwtProperty jwtProperty;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final MsgKeySelector msgKeySelector;
    @Autowired
    private DataSourceProperty dataSourceProperty;
    private final IdGenerator idGenerator;

    public List<RoomInfoDto> getUserRooms(Pageable pageable) {
        // 유저 챗룸 가져오기
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        User user = User.builder().id(userId).build();

        // roomInfoDtos를 채우기 위한 userChatRoom 리스트
        List<UserChatRoom> userChatRooms = mainRepositoryService.getUserChatRooms(user, pageable);


        HashMap<Long,RoomInfoDto> roomInfoDtos = new HashMap<>(50);
        ArrayList<Long> roomIds = new ArrayList<>();

        fillReturnDtoPart(userChatRooms, roomIds, roomInfoDtos);

        // 방안에 있는 유저수를 구하기 위한 in절 쿼리 연결되어 있는 유저챗룸 , 유저 한번에 join
        List<ChatRoom> roomList = mainRepositoryService.getRoomList(roomIds);

        // 방 안에 있는 유저 수 구하기
        for (ChatRoom room : roomList) {
            fillUserCountInRoom(room, roomInfoDtos);

            // 방 이름 없으면 --> 구해서 집어 넣자
            if (redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomName")==null) {

                String roomName = getRoomName(room);
                roomInfoDtos.get(room.getId()).setRoomName(roomName);

            }
            // 방 이미지 없으면 --> 구해서 집어 넣자
            if(redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomImageUrl")==null){
                //유저가 두명이라면
                int usersInRoom = room.getUserChatRooms().size();
                if (usersInRoom==2){
                    //상대 유저 이미지 url 가져오기
                    fillOpponentImage(room, userId, roomInfoDtos);

                }
                // 유저가 3명 이상이라면
                // 리스트 앞 순위 2명의 이미지 url 가져오기
                else if (usersInRoom>2){
                    StringBuilder sb = new StringBuilder();
                    int flag = 0;
                    for (int i = 0; i < usersInRoom; i++) {
                        
                        if (flag<1){
                            appendUrlInSb(room, i, sb);
                            flag++;
                        }else {
                            appendUrlInSb(room, i, sb);
                            setRoomImages(room, sb, roomInfoDtos);
                            break;
                        }
                        
                    }
                }


            }
        }

        // 안 읽은 메세지 수, 마지막 메세지 내용을 구하기 위함
        // HashMap을 루프돌면서 방 id와 떠난 시간을 구해서 새로운 Mao<Long,LocalDateTime>에 넣어준다
        HashMap<Long, ZonedDateTime> roomAndClosedAt1 = new HashMap<>();
        HashMap<Long, ZonedDateTime> roomAndClosedAt2 = new HashMap<>();
        HashMap<Long, ZonedDateTime> roomAndClosedAt3 = new HashMap<>();

        for (UserChatRoom userChatRoom : userChatRooms) {

            fillRoomAndClosedAt(userChatRoom,
                    roomAndClosedAt1,
                    roomAndClosedAt2,
                    roomAndClosedAt3);

        }

        fillJdbcInReturnDto(roomAndClosedAt1, roomAndClosedAt2, roomAndClosedAt3, roomInfoDtos);


        List<RoomInfoDto> roomInfoList = new ArrayList<>(roomInfoDtos.values());
        sortLastMsgDesc(roomInfoList);

        return roomInfoList;
    }

    private void sortLastMsgDesc(List<RoomInfoDto> roomInfoList) {
        roomInfoList.sort((o1, o2) -> {
            ZonedDateTime t1 = o1.getLastMessageTime() != null
                    ? o1.getLastMessageTime()
                    : LocalDateTime.MIN.atZone(ZoneOffset.UTC);
            ZonedDateTime t2 = o2.getLastMessageTime() != null
                    ? o2.getLastMessageTime()
                    : LocalDateTime.MIN.atZone(ZoneOffset.UTC);
            // 최신순
            return t2.compareTo(t1);
        });
    }

    private void fillJdbcInReturnDto(HashMap<Long, ZonedDateTime> roomAndClosedAt1, HashMap<Long, ZonedDateTime> roomAndClosedAt2, HashMap<Long, ZonedDateTime> roomAndClosedAt3, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        List<MessageJdbcDto> messageJdbcDtos1 = getMessageJdbcDtos(roomAndClosedAt1);
        List<MessageJdbcDto> messageJdbcDtos2 = getMessageJdbcDtos(roomAndClosedAt2);
        List<MessageJdbcDto> messageJdbcDtos3 = getMessageJdbcDtos(roomAndClosedAt3);

        fillRoomInfoDtos(messageJdbcDtos1, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos2, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos3, roomInfoDtos);
    }


    private List<MessageJdbcDto> getMessageJdbcDtos(HashMap<Long, ZonedDateTime> roomAndClosedAt1) {
        Long lea1 = getRandomId(roomAndClosedAt1);

        if (lea1 == 0L) {
            return List.of();
        } else {
            return messageRepositoryService.getAggregates(lea1, roomAndClosedAt1);
        }

    }

    
    
    private Long getRandomId(HashMap<Long, ZonedDateTime> roomAndClosedAt) {
        if(!roomAndClosedAt.isEmpty()){
            return roomAndClosedAt.keySet().iterator().next();
        }else{
            return 0L;
        }
    }

    
    
    private void fillRoomAndClosedAt(UserChatRoom userChatRoom,
                                     HashMap<Long, ZonedDateTime> roomAndClosedAt1, 
                                     HashMap<Long, ZonedDateTime> roomAndClosedAt2,
                                     HashMap<Long, ZonedDateTime> roomAndClosedAt3) {

        Long roomId = userChatRoom.getChatRoom().getId();

        if(msgKeySelector.getDbKey(roomId).equals(dataSourceProperty.getShardList().get(0).getKey())){
            roomAndClosedAt1.put(roomId, userChatRoom.getLeavedAt());
        }
        else if(msgKeySelector.getDbKey(roomId).equals(dataSourceProperty.getShardList().get(1).getKey())){
            roomAndClosedAt2.put(roomId, userChatRoom.getLeavedAt());
        }
        else{
            roomAndClosedAt3.put(roomId, userChatRoom.getLeavedAt());
        }
    }

    
    
    private void setRoomImages(ChatRoom room, StringBuilder sb, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        String image = sb.toString();
        redisTemplate.opsForValue().set("chatRoomId:" + room.getId() + ":roomImageUrl", image, 30, TimeUnit.MINUTES);
        roomInfoDtos.get(room.getId()).setRoomImageUrl(List.of(image.split(", ")));
    }

    
    
    private void appendUrlInSb(ChatRoom room, int i, StringBuilder sb) {
        
        if(i<1){
            String userImageUrl = getImageUrl(room.getUserChatRooms().get(i));
            sb.append(userImageUrl).append(", ");
        }else{
            String userImageUrl = getImageUrl(room.getUserChatRooms().get(i));
            sb.append(userImageUrl);
        }
        
    }

    
    
    private String getImageUrl(UserChatRoom room) {
        String userImageUrl = room.getUser().getUserImageUrl();
        return userImageUrl;
    }

    
    
    private void fillOpponentImage(ChatRoom room, Long userId, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        room.getUserChatRooms().forEach(userChatRoom -> {
            if (userChatRoom.getUser().getId() != userId) {
                String userImageUrl = getImageUrl(userChatRoom);
                roomInfoDtos.get(room.getId()).setRoomImageUrl(List.of(userImageUrl));
            }
        });
    }

    
    
    private void fillUserCountInRoom(ChatRoom room, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        int userCount = room.getUserChatRooms().size();
        roomInfoDtos.get(room.getId()).setJoinedUserCount(userCount);
    }

    
    
    private void fillReturnDtoPart(List<UserChatRoom> userChatRooms, ArrayList<Long> roomIds, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        for (UserChatRoom userChatRoom : userChatRooms) {

            roomIds.add(userChatRoom.getChatRoom().getId());
            String roomName = redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomName");
            String imageUrl = redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomImageUrl");


            // 해쉬맵에 [key : 방 id , value : 방 정보]저장
            roomInfoDtos.put(userChatRoom.getChatRoom().getId(), RoomInfoDto.builder()
                    .roomId(userChatRoom.getChatRoom().getId())
                    .leavedAt(userChatRoom.getLeavedAt()!=null ? userChatRoom.getLeavedAt() : null)
                    .roomName(roomName)
                    .roomImageUrl(imageUrl==null ? Collections.emptyList() : List.of(imageUrl.split(", ")))
                    .build());

        }
    }

    
    
    private String getRoomName(ChatRoom room) {
        List<UserChatRoom> usersInRoom = room.getUserChatRooms();
        StringBuilder sb = new StringBuilder();

        int flag = 0;
        for (int i = 0; i < usersInRoom.size(); i++) {
            if (flag<4){
                UserChatRoom userChatRoom = usersInRoom.get(i);
                sb.append(userChatRoom.getUser().getName());
                flag++;
            }

            if(flag==4||i==usersInRoom.size()-1){
                break;
            }else{
                sb.append(", ");
            }

        }
        String roomName = sb.toString();
        return roomName;
    }

    //messageJdbcDtos1,2,3의
    // private Long roomId;
    // private Integer unreadCount;
    // private String lastMessage;
    // private LocalDateTime lastMessageTime;
    //이 값들을 roomInfoDtos에 넣어준다
    private void fillRoomInfoDtos(List<MessageJdbcDto> messageJdbcDtos, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        for (MessageJdbcDto messageJdbcDto : messageJdbcDtos) {
            Long roomId = messageJdbcDto.getRoomId();
            int unreadCount = messageJdbcDto.getUnreadCount();
            String lastMessage = messageJdbcDto.getLastMessage();
            ZonedDateTime lastMessageTime = messageJdbcDto.getLastMessageTime();

            RoomInfoDto roomInfoDto = roomInfoDtos.get(roomId);
            if (roomInfoDto != null) {
                roomInfoDto.setUnreadCount(unreadCount);
                roomInfoDto.setLastMessage(lastMessage);
                roomInfoDto.setLastMessageTime(lastMessageTime);
            }
        }
    }

    
    
    public void leaveRoom(Long roomId) {
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        mainRepositoryService.updateLeavedAtToNow(roomId,userId);
    }

    
    
    public void joinRoom(Long roomId) {
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        mainRepositoryService.updateLeavedAtToNull(roomId, userId);
    }

    
    
    public RoomDetailsDto getRoomDetails(Long roomId) {
        ChatRoom room = mainRepositoryService.findRoom(roomId);
        String roomName = room.getRoomName();
        String roomImageUrl = room.getRoomImageUrl();
        String finalRoomName = (roomName ==null)? getRoomName(room) : roomName;
        String roomImage = roomImageUrl==null?getRoomImage(room): roomImageUrl;
        int usersInRoom = room.getUserChatRooms().size();
        String notification = room.getNotification();
        Long id = room.getId();

        return RoomDetailsDto.builder()
                .roomName(finalRoomName)
                .roomImageUrl(roomImage.contains(", ")? List.of(roomImage.split(", ")) : List.of(roomImage))
                .joinedUserCount(usersInRoom)
                .notification(notification)
                .roomId(id)
                .build();

    }

    
    
    public String getRoomImage(ChatRoom room) {

        String imageUrl = redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomImageUrl");
        if(imageUrl ==null){

            int usersInRoom = room.getUserChatRooms().size();
            Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
            StringBuilder sb = new StringBuilder();

            if (usersInRoom==2){
                //상대 유저 이미지 url 가져오기
                room.getUserChatRooms().forEach(userChatRoom -> {
                    if (userChatRoom.getUser().getId() != userId) {
                        sb.append(userChatRoom.getUser().getUserImageUrl());
                    }
                });

            }
            // 유저가 3명 이상이라면
            // 리스트 앞 순위 2명의 이미지 url 가져오기
            else if (usersInRoom>2){
                int flag = 0;
                for (int i = 0; i < usersInRoom; i++) {
                    if (flag<1){
                        appendUrlInSb(room, i, sb);
                        flag++;
                    }else {
                        String userImageUrl = getImageUrl(room.getUserChatRooms().get(i));
                        sb.append(userImageUrl);
                        break;
                    }
                }
            }
            return sb.toString();
        }else{
            return imageUrl;
        }

    }



    public void createRoom(RoomCreateDto roomCreateDto) {

        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        List<Long> userIds = roomCreateDto.getOtherUserIds();
        userIds.add(userId);

        Long chatRoomId = idGenerator.nextId();

        ChatRoom chatRoom = ChatRoom.builder()
                .id(chatRoomId)
                .roomName(roomCreateDto.getRoomName())
                .roomImageUrl(roomCreateDto.getRoomImageUrl())
                .createdAt(ZonedDateTime.now())
                .lastMessageId(0L)
                .build();

        List<UserChatRoom> userChatRooms = new ArrayList<>();

        for (Long id : userIds) {
            UserChatRoom userChatRoom = UserChatRoom.builder()
                    .id(idGenerator.nextId())
                    .user(User.builder().id(id).build())
                    .chatRoom(chatRoom)
                    .isJoined(false)
                    .lastReadMessageId(0L)
                    .build();
            userChatRooms.add(userChatRoom);
        }

        mainRepositoryService.saveUserChatRooms(chatRoom,userChatRooms);
        storeRoomAndMsgFirstId(userIds, chatRoomId);

    }

    private void storeRoomAndMsgFirstId(List<Long> userIds, Long chatRoomId) {
        for (Long userId : userIds) {
            redisService.putUserLastMsgIdInRoom(userId, chatRoomId, "0");
        }
    }


    public List<RoomMessageListDto> getRoomMessages(Long roomId, Pageable pageable) {

        // 방 --> 메세지 리스트
        List<Message> messages = messageRepositoryService.getMessages(roomId, pageable);

        // jwt --> userId
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());

        // 방 --> 유저 챗룸 (Join user)
        List<UserChatRoom> userChatRoomList = mainRepositoryService.findUserChatRoomList(roomId);


        //가져온 메세지 리스트를 dto로 변환
        List<RoomMessageListDto> returnList = messages.stream().map(message -> {
            RoomMessageListDto dto = RoomMessageListDto.builder()
                    .message(message.getMessage())
                    .createdAt(message.getCreatedAt())
                    .isMe(message.getSenderId().equals(userId))
                    .build();


            for (UserChatRoom userChatRoom : userChatRoomList) {

                ZonedDateTime userExitTime = (userChatRoom.getLeavedAt() == null) ? ZonedDateTime.of(LocalDateTime.MAX, ZoneId.systemDefault()) : userChatRoom.getLeavedAt();

                // 메세지 보낸 시간과 유저가 나간 시간 비교
                if (message.getCreatedAt().isAfter(userExitTime)) {
                    dto.setUnreadCount(dto.getUnreadCount() + 1);
                }
                // 메세지 보낸 유저와 방에 있는 유저 비교
                if (message.getSenderId().equals(userChatRoom.getUser().getId())) {
                    dto.setSenderImageUrl(userChatRoom.getUser().getUserImageUrl());
                    dto.setSenderName(userChatRoom.getUser().getName());
                }
            }
            return dto;
        }).toList();

        return returnList;
    }
}
