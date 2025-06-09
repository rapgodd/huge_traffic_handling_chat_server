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
import com.giyeon.chat_server.service.msgSender.JoinMsgSenderService;
import com.giyeon.chat_server.service.redisService.RedisService;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.IdDistributionUtils;
import com.giyeon.chat_server.util.JwtUtil;
import com.giyeon.chat_server.ws.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final MainRepositoryService mainRepositoryService;
    private final MessageRepositoryService messageRepositoryService;
    private final JwtProperty jwtProperty;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final MsgKeySelector msgKeySelector;
    @Autowired
    private DataSourceProperty dataSourceProperty;
    private final IdGenerator idGenerator;
    private final SessionRegistry sessionRegistry;
    private final RedisService redisService;
    private final JoinMsgSenderService joinMsgSenderService;

    public List<RoomInfoDto> getUserRooms(Pageable pageable) {
        // 유저 챗룸 가져오기
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());

        // roomInfoDtos를 채우기 위한 userChatRoom 리스트
        List<UserChatRoom> userChatRooms = mainRepositoryService.getUserChatRooms(userId, pageable);


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

                String roomName = getRoomName(room, userId);
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
        HashMap<Long, Long> roomAndLastReadMsgId1 = new HashMap<>();
        HashMap<Long, Long> roomAndLastReadMsgId2 = new HashMap<>();
        HashMap<Long, Long> roomAndLastReadMsgId3 = new HashMap<>();

        for (UserChatRoom userChatRoom : userChatRooms) {

            fillRoomAndLastMsgId(userChatRoom,
                    roomAndLastReadMsgId1,
                    roomAndLastReadMsgId2,
                    roomAndLastReadMsgId3);

        }

        fillJdbcInReturnDto(roomAndLastReadMsgId1, roomAndLastReadMsgId2, roomAndLastReadMsgId3, roomInfoDtos);


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

    private void fillJdbcInReturnDto(HashMap<Long, Long> roomAndLastMsgId1, HashMap<Long, Long> roomAndLastMsgId2, HashMap<Long, Long> roomAndLastMsgId3, HashMap<Long, RoomInfoDto> roomInfoDtos) {
        List<MessageJdbcDto> messageJdbcDtos1 = getMessageJdbcDtos(roomAndLastMsgId1);
        List<MessageJdbcDto> messageJdbcDtos2 = getMessageJdbcDtos(roomAndLastMsgId2);
        List<MessageJdbcDto> messageJdbcDtos3 = getMessageJdbcDtos(roomAndLastMsgId3);

        fillRoomInfoDtos(messageJdbcDtos1, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos2, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos3, roomInfoDtos);
    }


    private List<MessageJdbcDto> getMessageJdbcDtos(HashMap<Long, Long> roomAndLastReadMsgId) {
        Long lea1 = getRandomId(roomAndLastReadMsgId);

        if (lea1 == 0L) {
            return List.of();
        } else {
            return messageRepositoryService.getAggregates(lea1, roomAndLastReadMsgId);
        }

    }

    
    
    private Long getRandomId(HashMap<Long, Long> roomAndLastReadMsgId) {
        if(!roomAndLastReadMsgId.isEmpty()){
            return roomAndLastReadMsgId.keySet().iterator().next();
        }else{
            return 0L;
        }
    }

    
    
    private void fillRoomAndLastMsgId(UserChatRoom userChatRoom,
                                     HashMap<Long, Long> roomAndLastReadMsgId1,
                                     HashMap<Long, Long> roomAndLastReadMsgId2,
                                     HashMap<Long, Long> roomAndLastReadMsgId3) {

        Long roomId = userChatRoom.getChatRoom().getId();

        if(msgKeySelector.getDbKey(roomId).equals(dataSourceProperty.getShardList().get(0).getKey())){
            roomAndLastReadMsgId1.put(roomId, userChatRoom.getLastReadMessageId());
        }
        else if(msgKeySelector.getDbKey(roomId).equals(dataSourceProperty.getShardList().get(1).getKey())){
            roomAndLastReadMsgId2.put(roomId, userChatRoom.getLastReadMessageId());
        }
        else{
            roomAndLastReadMsgId3.put(roomId, userChatRoom.getLastReadMessageId());
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
            String roomName = (String) redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomName");
            String imageUrl = (String) redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomImageUrl");


            // 해쉬맵에 [key : 방 id , value : 방 정보]저장
            roomInfoDtos.put(userChatRoom.getChatRoom().getId(), RoomInfoDto.builder()
                    .roomId(userChatRoom.getChatRoom().getId())
                    .roomName(roomName)
                    .roomImageUrl(imageUrl==null ? Collections.emptyList() : List.of(imageUrl.split(", ")))
                    .build());

        }
    }

    
    
    private String getRoomName(ChatRoom room, Long userId) {
        List<UserChatRoom> usersInRoom = room.getUserChatRooms();
        StringBuilder sb = new StringBuilder();

        int flag = 0;
        for (int i = 0; i < usersInRoom.size(); i++) {
            if (flag<4){
                UserChatRoom userChatRoom = usersInRoom.get(i);
                // 방에 있는 유저가 나일 경우
                if (userChatRoom.getUser().getId() == userId) {
                    flag++;
                    continue;
                }else {
                    // 방에 있는 유저가 나와 다를 경우
                    sb.append(userChatRoom.getUser().getName());
                    flag++;
                }
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
        redisService.removeJoinedUserInRoom(userId, roomId);
        mainRepositoryService.removeJoinedUserInRoom(roomId, userId);
    }

    
    
    public void joinRoom(Long roomId) {

        // 마지막 메세지 id 가져와서 세션 전송
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        Long lastReadMsgId = redisService.getUserLastReadMsgIdInRoom(userId, roomId);

        // grpc또는 세션 전송
        // 방에 있는 유저 set 다 가져오기
        List<Long> currentJoinedUsers = redisService.getCurrentJoinedUsers(roomId);

        List<Long> localSessionUsersList = new ArrayList<>(30);
        List<Long> remoteSessionUsersList = new ArrayList<>(30);

        IdDistributionUtils.distributeRemoteAndLocal(currentJoinedUsers, localSessionUsersList,
                remoteSessionUsersList, sessionRegistry, redisTemplate);

        // 로컬 전송
        joinMsgSenderService.sendJoinMsgToLocal(localSessionUsersList, userId, lastReadMsgId);
        // 원격 전송
        joinMsgSenderService.sendJoinMsgToRemote(remoteSessionUsersList, roomId, userId, lastReadMsgId);
        // 유저 리스트에 추가
        redisService.addCurrentJoinedUser(userId, roomId);
        // 마지막 메세지 가져오기
        Long roomLastMsgId = redisService.getLastMsgIdInRoom(roomId);
        // redis & user 저장
        redisService.putUserLastMsgIdInRoom(userId, roomId, String.valueOf(roomLastMsgId));
    }


    public RoomDetailsDto getRoomDetails(Long roomId) {
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        ChatRoom room = mainRepositoryService.findRoom(roomId);
        String roomName = room.getRoomName();
        String roomImageUrl = room.getRoomImageUrl();
        String finalRoomName = (roomName ==null)? getRoomName(room,userId) : roomName;
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

        String imageUrl = (String) redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomImageUrl");
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

        Long currentUserId = JwtUtil.getUserId(jwtProperty.getSecret());

        List<UserChatRoom> userChatRooms = mainRepositoryService.findUserChatRoomList(roomId);

        Map<Long, Long> lastReadByUser = new HashMap<>();
        Map<Long, User> userById = new HashMap<>();
        for (UserChatRoom userChatRoom : userChatRooms) {
            lastReadByUser.put(userChatRoom.getUser().getId(), Optional.ofNullable(userChatRoom.getLastReadMessageId()).orElse(0L));
            userById.put(userChatRoom.getUser().getId(), userChatRoom.getUser());
        }


        List<Message> messages = messageRepositoryService.getMessages(roomId, pageable);

        List<RoomMessageListDto> roomMessageListDtos = messages.stream().map(msg -> {
            boolean isMe = msg.getSenderId().equals(currentUserId);


            int unreadCount = (int) lastReadByUser.values().stream()
                    .filter(lastReadId -> msg.getId() > lastReadId)
                    .count();


            User sender = userById.get(msg.getSenderId());

            return RoomMessageListDto.builder()
                    .message(msg.getMessage())
                    .createdAt(msg.getCreatedAt())
                    .isMe(isMe)
                    .unreadCount(unreadCount)
                    .senderName(sender.getName())
                    .senderImageUrl(sender.getUserImageUrl())
                    .build();
        }).collect(Collectors.toList());


        Collections.sort(roomMessageListDtos, (o1, o2) -> {
            ZonedDateTime t1 = o1.getCreatedAt();
            ZonedDateTime t2 = o2.getCreatedAt();
            return t1.compareTo(t2);
        });
        return roomMessageListDtos;
    }

    public void closeRoom(Long roomId) {
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        redisService.removeJoinedUserInRoom(userId, roomId);
        mainRepositoryService.setUserChatRoomToClose(roomId, userId);
    }
}
