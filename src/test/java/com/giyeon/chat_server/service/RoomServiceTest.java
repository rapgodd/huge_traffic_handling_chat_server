package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.RoomCreateDto;
import com.giyeon.chat_server.dto.RoomMessageListDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.Role;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import com.giyeon.chat_server.repository.main.UserRepository;
import com.giyeon.chat_server.repository.message.MessageRepository;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;


@SpringBootTest
class RoomServiceTest {

    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserChatRoomRepository userChatRoomRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private MessageRepositoryService messageService;
    private static MockedStatic<JwtUtil> jwtUtilMockedStatic;

    @BeforeAll
    static void beforeAll() {
        jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        jwtUtilMockedStatic.when(() -> JwtUtil.getUserId(anyString())).thenReturn(1L);
    }

    @Test
    @Tag("createRoom")
    void createRoom(){
        // given
        User user1 = User.builder()
                .id(1L)
                .name("user1")
                .userRole(Role.ROLE_USER)
                .build();
        userRepository.save(user1);
        User user2 = User.builder()
                .id(2L)
                .name("user2")
                .userRole(Role.ROLE_USER)
                .build();
        userRepository.save(user2);

        RoomCreateDto newRoom = RoomCreateDto.builder()
                .otherUserIds(new ArrayList<>(List.of(2L)))
                .roomName("test room")
                .roomImageUrl("test image url")
                .build();

        roomService.createRoom(newRoom);

        // when
        ChatRoom testRoom = roomRepository.findByRoomName("test room");
        String lastMessageId = (String)redisTemplate.opsForHash().get("user:1:roomAndLastMsgId", testRoom.getId().toString());

        // then
        // 1. 방이 잘 생성되었는지 확인
        Assertions.assertEquals("test room", testRoom.getRoomName());
        // 2. 방에 유저들이 잘 들어갔는지 확인
        Assertions.assertTrue(testRoom.getUserChatRooms().size()==2);
        // 3. leavedAt null인지 확인
        Assertions.assertNull(testRoom.getUserChatRooms().get(0).getLeavedAt());
        Assertions.assertNull(testRoom.getUserChatRooms().get(1).getLeavedAt());
        // 4. redis에 user:{userId}:roomAndLastMsgId 에 roomId:LastMsgId가 0으로 잘 들어갔는지 확인
        Assertions.assertEquals("0", lastMessageId);

    }

    @Test
    @Tag("getRoomMessages")
    void getRoomMessages(){
        // given
        // 유저 생성
        User user1 = User.builder()
                .id(1L)
                .name("user1")
                .userRole(Role.ROLE_USER)
                .build();
        userRepository.save(user1);
        User user2 = User.builder()
                .id(2L)
                .userRole(Role.ROLE_USER)
                .name("user2")
                .build();
        userRepository.save(user2);

        // 방 생성
        RoomCreateDto createDto = RoomCreateDto.builder()
                .otherUserIds(new ArrayList<>(List.of(2L)))
                .roomName("testRoom")
                .roomImageUrl("img")
                .build();
        roomService.createRoom(createDto);

        // 1L의 유저 챗룸을 찾아서 isjoined true로 바꿔줌
        ChatRoom room = roomRepository.findByRoomName("testRoom");
        Long roomId = room.getId();
        UserChatRoom userChatRoom = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, roomId);
        userChatRoom.updateIsJoined(true);

        // 메시지 60개 저장
        Long lastMsgId = 0L;
        for (int i = 1; i <= 60; i++) {
            Message msg = Message.builder()
                    .id(idGenerator.nextId())
                    .roomId(roomId)
                    .senderId(i % 2 == 0 ? 1L : 2L)
                    .message("msg" + i)
                    .createdAt(ZonedDateTime.now().plusSeconds(i))
                    .build();
            messageService.save(roomId,msg);
            if (i==60) {
                lastMsgId = msg.getId();
            }
        }
        userChatRoom.updateNewMsgId(lastMsgId);
        userChatRoomRepository.save(userChatRoom);


        // when
        Pageable pageable = PageRequest.of(0, 50);
        List<RoomMessageListDto> result = roomService.getRoomMessages(roomId, pageable);
        // then
        Assertions.assertEquals(50, result.size());
        for (int i = 1; i < result.size(); i++) {

            Assertions.assertTrue(
                    result.get(i).getCreatedAt().isAfter(result.get(i - 1).getCreatedAt())
            );

            if (result.get(i).getSenderName().equals("user1")) {
                Assertions.assertTrue(result.get(i).isMe());
            } else {
                Assertions.assertFalse(result.get(i).isMe());
            }

        }
        Assertions.assertTrue(result.stream().allMatch(dto -> dto.getUnreadCount() == 1));

    }


    @AfterEach
    void cleanup(TestInfo testInfo) {
        if (testInfo.getTags().contains("createRoom")) {
            messageRepository.deleteAll();
            userChatRoomRepository.deleteAll();
            userRepository.deleteAll();
            roomRepository.deleteAll();
            redisTemplate.delete("user:1:roomAndLastMsgId");
            redisTemplate.delete("user:2:roomAndLastMsgId");

        }
        if (testInfo.getTags().contains("getRoomMessages")) {
            messageRepository.deleteAll();
            userChatRoomRepository.deleteAll();
            userRepository.deleteAll();
            roomRepository.deleteAll();

        }
    }

    @AfterAll
    static void afterAll() {
        jwtUtilMockedStatic.close();
    }

}