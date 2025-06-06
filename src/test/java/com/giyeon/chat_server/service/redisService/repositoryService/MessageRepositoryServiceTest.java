package com.giyeon.chat_server.service.redisService.repositoryService;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.MessageJdbcDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.Role;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import com.giyeon.chat_server.repository.main.UserRepository;
import com.giyeon.chat_server.repository.message.MessageRepository;
import com.giyeon.chat_server.service.RoomService;
import com.giyeon.chat_server.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class MessageRepositoryServiceTest {

    private static MockedStatic<JwtUtil> jwtUtilMockedStatic;

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserChatRoomRepository userChatRoomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private MessageRepositoryService messageService;

    @BeforeAll
    static void beforeAll() {
        jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        jwtUtilMockedStatic.when(() -> JwtUtil.getUserId(anyString())).thenReturn(1L);
    }



    @Test
    @Tag("getRoomInfo")
    void getAggregates() {
        // given
        // 1. user 1,2,3 생성
        User user1 = createAndSaveUser(1L, "user1");
        User user2 = createAndSaveUser(2L, "user2");
        User user3 = createAndSaveUser(3L, "user3");


        // 2. ChatRoom 2개 생성
        ChatRoom chatRoom = createAndSaveChatRoom(1L, "testRoom");
        ChatRoom chatRoom2 = createAndSaveChatRoom(5L, "testRoom2");

        // 3. 첫번째 ChatRoom에는 유저 1과 2 참여
        UserChatRoom userChatRoom1 = createUserChatRom(user1, chatRoom, 1L);
        UserChatRoom userChatRoom2 = createUserChatRom(user2, chatRoom, 5L);

        // 4. 두번째 ChatRoom에는 유저 2와 3 참여
        createUserChatRom(user2, chatRoom2, idGenerator.nextId());
        createUserChatRom(user3, chatRoom2, idGenerator.nextId());


        // 5. 각각의 ChatRoom에 메세지 생성 50개 하기
        // 6. 각각 메세지 40번째 id 따로 저장
        // 7. 50번째 메세지, 생성시간 따로 저장하기
        Long chatRoom40messageId = 0L;
        Long secChatRoom40messageId = 0L;

        String chatRoomLastMsg = "";

        ZonedDateTime chatRoomLastMsgCreatedAt = ZonedDateTime.now();

        ZonedDateTime randomMsgTime = ZonedDateTime.now();

        /**
         * 기준시 생성
         */
        LocalDateTime base = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
        for (int i = 0; i < 50; i++) {
            Message message = Message.builder()
                    .id(idGenerator.nextId())
                    .roomId(chatRoom.getId())
                    .senderId(user1.getId())
                    .message("메세지" + i)
                    .createdAt(ZonedDateTime.of(base,ZoneId.of("UTC")).plusSeconds(i))
                    .build();
            if (i == 40) {
                chatRoom40messageId = message.getId();
            }
            // 마지막 메세지 저장
            if(i==48){
                randomMsgTime = message.getCreatedAt();
            }

            if (i == 49) {
                chatRoomLastMsg = message.getMessage();
                chatRoomLastMsgCreatedAt = message.getCreatedAt();
            }
            messageRepository.save(message);
        }
        for (int i = 0; i < 50; i++) {
            Message message = Message.builder()
                    .id(idGenerator.nextId())
                    .roomId(chatRoom2.getId())
                    .senderId(user2.getId())
                    .message("메세지" + i)
                    .createdAt(ZonedDateTime.of(base,ZoneId.of("UTC")).plusSeconds(i))
                    .build();
            if (i == 40) {
                secChatRoom40messageId = message.getId();
            }
            // 마지막 메세지 저장
            messageRepository.save(message);
        }

        HashMap<Long,Long> userChatRoomIdAndLstMsgId = new HashMap<>();
        userChatRoomIdAndLstMsgId.put(userChatRoom1.getId(), chatRoom40messageId);
        userChatRoomIdAndLstMsgId.put(userChatRoom2.getId(), secChatRoom40messageId);

        // when
        // getAggregate 호출
        List<MessageJdbcDto> messageJdbcDtos = messageService.getAggregates(chatRoom.getId(), userChatRoomIdAndLstMsgId);

        //Then
        // 리턴 사이즈 2인지 확인
        assertEquals(2, messageJdbcDtos.size());
        // 방 id 제대로 들어가는지 확인
        assertEquals(chatRoom.getId(), messageJdbcDtos.get(0).getRoomId());
        // unreadCount 10인지 확인
        assertEquals(9, messageJdbcDtos.get(0).getUnreadCount());
        // 50번째 메세지 일치하는지 확인
        assertEquals(chatRoomLastMsg, messageJdbcDtos.get(0).getLastMessage());
        // 50번째 메세지 생성시간 일치하는지 확인
        LocalDateTime expectedLdt1 =
                chatRoomLastMsgCreatedAt
                        .toLocalDateTime()
                        .truncatedTo(ChronoUnit.SECONDS);

        LocalDateTime randomLdt =
                randomMsgTime
                        .toLocalDateTime()
                        .truncatedTo(ChronoUnit.SECONDS);

        ZonedDateTime lastMessageTime = messageJdbcDtos.get(0).getLastMessageTime();

        LocalDateTime expectedLdt2 =
                lastMessageTime
                        .toLocalDateTime()
                        .truncatedTo(ChronoUnit.SECONDS);

        assertEquals(expectedLdt1, expectedLdt2);
        assertFalse(randomLdt.isEqual(expectedLdt2));
    }

    private UserChatRoom createUserChatRom(User user, ChatRoom chatRoom, long id) {
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .isJoined(true)
                .lastReadMessageId(0L)
                .id(id)
                .build();
        userChatRoomRepository.save(userChatRoom);
        return userChatRoom;
    }

    private ChatRoom createAndSaveChatRoom(long id, String testRoom) {
        ChatRoom chatRoom = ChatRoom.builder()
                .id(id)
                .roomName(testRoom)
                .roomImageUrl("img")
                .lastMessageId(0L)
                .createdAt(ZonedDateTime.now())
                .build();
        roomRepository.save(chatRoom);
        return chatRoom;
    }

    private User createAndSaveUser(long id, String user) {
        User result = User.builder()
                .id(id)
                .name(user)
                .userRole(Role.ROLE_USER)
                .build();
        userRepository.save(result);
        return result;
    }

    @AfterAll
    static void afterAll() {
        jwtUtilMockedStatic.close();
    }

    @AfterEach
    void cleanup(TestInfo testInfo) {
        if (testInfo.getTags().contains("getRoomInfo")) {
            messageRepository.deleteAll();
            userChatRoomRepository.deleteAll();
            userRepository.deleteAll();
            roomRepository.deleteAll();

        }
    }


    }