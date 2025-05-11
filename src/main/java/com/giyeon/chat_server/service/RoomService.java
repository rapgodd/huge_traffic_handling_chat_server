package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.MsgKeySelector;
import com.giyeon.chat_server.dto.MessageJdbcDto;
import com.giyeon.chat_server.dto.RoomInfoDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.giyeon.chat_server.properties.JwtProperty;
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


    public List<RoomInfoDto> getUserRooms(Pageable pageable) {
        // 유저 챗룸 가져오기
        Long userId = JwtUtil.getUserId(jwtProperty.getSecret());
        User user = User.builder().id(userId).build();

        // roomInfoDtos를 채우기 위한 userChatRoom 리스트
        List<UserChatRoom> userChatRooms = mainRepositoryService.getUserChatRooms(user, pageable);


        HashMap<Long,RoomInfoDto> roomInfoDtos = new HashMap<>(50);
        ArrayList<Long> roomIds = new ArrayList<>();

        for (UserChatRoom userChatRoom : userChatRooms) {

            roomIds.add(userChatRoom.getChatRoom().getId());
            String roomName = redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomName");
            String imageUrl = redisTemplate.opsForValue().get("chatRoomId:" + userChatRoom.getChatRoom().getId() + ":roomImageUrl");


            // 해쉬맵에 [key : 방 id , value : 방 정보]저장
            roomInfoDtos.put(userChatRoom.getChatRoom().getId(), RoomInfoDto.builder()
                    .roomId(userChatRoom.getChatRoom().getId())
                    .leavedAt(userChatRoom.getLeavedAt()!=null ? userChatRoom.getLeavedAt() : userChatRoom.getChatRoom().getCreatedAt())
                    .lastMessageTime(userChatRoom.getChatRoom().getLastMessageTime())
                    .roomName(roomName)
                    .roomImageUrl(imageUrl==null ? Collections.emptyList() : List.of(imageUrl.split(", ")))
                    .build());

        }

        // 방안에 있는 유저수를 구하기 위한 in절 쿼리 연결되어 있는 유저챗룸 , 유저 한번에 join
        List<ChatRoom> roomList = mainRepositoryService.getRoomList(roomIds);

        // 방 안에 있는 유저 수 구하기
        for (ChatRoom room : roomList) {
            int userCount = room.getUserChatRooms().size();
            roomInfoDtos.get(room.getId()).setJoinedUserCount(userCount);

            // 방 이름 없으면 --> 구해서 집어 넣자
            if (redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomName")==null) {
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
                redisTemplate.opsForValue().set("chatRoomId:" + room.getId() + ":roomName", roomName, 30, TimeUnit.MINUTES);
                roomInfoDtos.get(room.getId()).setRoomName(roomName);

            }
            // 방 이미지 없으면 --> 구해서 집어 넣자
            if(redisTemplate.opsForValue().get("chatRoomId:" + room.getId() + ":roomImageUrl")==null){
                //유저가 두명이라면
                int usersInRoom = room.getUserChatRooms().size();
                if (usersInRoom==2){
                    //상대 유저 이미지 url 가져오기
                    room.getUserChatRooms().forEach(userChatRoom -> {
                        if (userChatRoom.getUser().getId() != userId) {
                            redisTemplate.opsForValue().set("chatRoomId:" + room.getId() + ":roomImageUrl", userChatRoom.getUser().getUserImageUrl(), 30, TimeUnit.MINUTES);
                            roomInfoDtos.get(room.getId()).setRoomImageUrl(List.of(userChatRoom.getUser().getUserImageUrl()));
                        }
                    });

                }
                // 유저가 3명 이상이라면
                // 리스트 앞 순위 2명의 이미지 url 가져오기
                else if (usersInRoom>2){
                    StringBuilder sb = new StringBuilder();
                    int flag = 0;
                    for (int i = 0; i < usersInRoom; i++) {
                        if (flag<1){
                            String userImageUrl = room.getUserChatRooms().get(i).getUser().getUserImageUrl();
                            sb.append(userImageUrl).append(", ");
                            flag++;
                        }else {
                            String userImageUrl = room.getUserChatRooms().get(i).getUser().getUserImageUrl();
                            sb.append(userImageUrl);
                            String image = sb.toString();
                            redisTemplate.opsForValue().set("chatRoomId:" + room.getId() + ":roomImageUrl", image, 30, TimeUnit.MINUTES);
                            roomInfoDtos.get(room.getId()).setRoomImageUrl(List.of(image.split(", ")));
                            break;
                        }
                    }
                }
            }
        }

        // 안 읽은 메세지 수, 마지막 메세지 내용을 구하기 위함
        // HashMap을 루프돌면서 방 id와 떠난 시간을 구해서 새로운 Mao<Long,LocalDateTime>에 넣어준다
        HashMap<Long, LocalDateTime> leavedAtByRoom1 = new HashMap<>();
        HashMap<Long, LocalDateTime> leavedAtByRoom2 = new HashMap<>();
        HashMap<Long, LocalDateTime> leavedAtByRoom3 = new HashMap<>();

        for (UserChatRoom userChatRoom : userChatRooms) {
            Long roomId = userChatRoom.getChatRoom().getId();

            String shard1 = dataSourceProperty.getShardList().get(0).getKey();
            String shard2 = dataSourceProperty.getShardList().get(1).getKey();

            if(msgKeySelector.getDbKey(roomId).equals(shard1)){
                leavedAtByRoom1.put(roomId, userChatRoom.getLeavedAt());
            }
            else if(msgKeySelector.getDbKey(roomId).equals(shard2)){
                leavedAtByRoom2.put(roomId, userChatRoom.getLeavedAt());
            }
            else{
                leavedAtByRoom3.put(roomId, userChatRoom.getLeavedAt());
            }
        }

        //leavedAtByRoom1 첫번재 엔트리의 키 값을 가져온다
        Long lea1 = leavedAtByRoom1.keySet().iterator().next();
        List<MessageJdbcDto> messageJdbcDtos1 = messageRepositoryService.getAggregates(lea1, leavedAtByRoom1);

        //leavedAtByRoom2 첫번재 엔트리의 키 값을 가져온다
        Long lea2 = leavedAtByRoom2.keySet().iterator().next();
        List<MessageJdbcDto> messageJdbcDtos2 = messageRepositoryService.getAggregates(lea2, leavedAtByRoom2);

        //leavedAtByRoom3 첫번재 엔트리의 키 값을 가져온다
        Long lea3 = leavedAtByRoom3.keySet().iterator().next();
        List<MessageJdbcDto> messageJdbcDtos3 = messageRepositoryService.getAggregates(lea3, leavedAtByRoom3);

        fillRoomInfoDtos(messageJdbcDtos1, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos2, roomInfoDtos);
        fillRoomInfoDtos(messageJdbcDtos3, roomInfoDtos);

        //roomInfoDtos의 값을 List<RoomInfoDto>로 변환해 리턴
        List<RoomInfoDto> roomInfoList = new ArrayList<>(roomInfoDtos.values());
        //roomInfoList를 최신순으로 정렬
        roomInfoList.sort((o1, o2) -> o2.getLastMessageTime().compareTo(o1.getLastMessageTime()));
        //roomInfoList를 리턴
        return roomInfoList;
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
            LocalDateTime lastMessageTime = messageJdbcDto.getLastMessageTime();

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
        mainRepositoryService.updateLeavedAt(roomId,userId);
    }
}
