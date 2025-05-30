package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.Role;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.entity.message.Message;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import com.giyeon.chat_server.repository.main.UserRepository;
import com.giyeon.chat_server.repository.message.MessageRepository;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    private MessageService messageService;
    private static MockedStatic<JwtUtil> jwtUtilMockedStatic;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MessageRepositoryService messageRepositoryService;
    @Autowired
    private UserChatRoomRepository userChatRoomRepository;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MainRepositoryService mainRepositoryService;


    @BeforeAll
    static void beforeAll() {
        jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        jwtUtilMockedStatic.when(() -> JwtUtil.getUserId(anyString())).thenReturn(1L);
    }

    @Test
    void sendMessage() throws InterruptedException {

        //given
        // 1. 유저 두명 생성
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");
        // 2. 방과 유저챗룸 생성
        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        // 3. redis에 userLastMessageId와 RoomLastMessageId설정
        initializeRedis(room.getId(), user1.getId(), user2.getId());
        ChatDto chat = ChatDto.builder()
                .message("hello this is test")
                .senderName("user1")
                .senderId(1L)
                .roomId(1L)
                .build();

        //when
        // 1. sendMessage호출
        messageService.sendMessage(chat);
        Thread.sleep(1000);
        Pageable paging = PageRequest.of(0, 50);
        Message message = messageRepositoryService.getMessages(room.getId(), paging).get(0);
        String lastMsgId = (String) redisTemplate.opsForHash().get("user:" + user1.getId() + ":roomAndLastMsgId", "1");
        String lastMsgId1 = (String) redisTemplate.opsForHash().get("user:" + user2.getId() + ":roomAndLastMsgId", "1");
        String roomLastMsgId = (String) redisTemplate.opsForValue().get("room:" + room.getId() + ":lastMsgId");
        List<UserChatRoom> all = userChatRoomRepository.findAll();
        ChatRoom chatRoom = mainRepositoryService.findRoom(room.getId());


        //then
        //1.db에 잘 저장되어 있는지 확인
        assertThat(message).isNotNull();
        //2. message 시간 잘 생성되었는지 확인
        assertThat(message.getCreatedAt()).isAfter(ZonedDateTime.now().minusMinutes(10));
        //3. senderId, 메세지 잘 저장되었는지 확인
        assertThat(message.getMessage()).isEqualTo("hello this is test");
        assertThat(message.getSenderId()).isEqualTo(1L);
        //4. redis에 LastMsgId잘 반영되었는지 확인
        assertThat(lastMsgId).isEqualTo(String.valueOf(message.getId()));
        assertThat(lastMsgId1).isEqualTo(String.valueOf(message.getId()));
        assertThat(roomLastMsgId).isEqualTo(String.valueOf(message.getId()));
        //5. userChatRoom 잘 Update되었는지 확인
        assertThat(all).extracting(UserChatRoom::getLastReadMessageId).allMatch(a -> a.equals(message.getId()));
        //6. chatRoomLastMsgId 업데이트 되었는지 확인
        assertThat(chatRoom.getLastMessageId()).isEqualTo(message.getId());






    }


    private User createUser(Long id, String name) {
        User u = User.builder()
                .id(id)
                .name(name)
                .email(name + "@example.com")
                .userImageUrl("https://example.com/" + name + ".jpg")
                .password("pass")
                .userRole(Role.ROLE_USER)
                .build();
        return userRepository.save(u);
    }

    private ChatRoom createRoomWithUsers(Long roomId, User... users) {
        ChatRoom room = ChatRoom.builder()
                .id(roomId)
                .lastMessageId(0L)
                .createdAt(ZonedDateTime.now())
                .build();
        roomRepository.save(room);

        for (User u : users) {
            UserChatRoom ucr = UserChatRoom.builder()
                    .id(idGenerator.nextId())
                    .user(u)
                    .chatRoom(room)
                    .isJoined(true)
                    .leavedAt(null)
                    .lastReadMessageId(0L)
                    .build();
            userChatRoomRepository.save(ucr);
        }
        return room;
    }

    private void initializeRedis(Long roomId, Long... userIds) {
        String roomKey = "room:" + roomId + ":lastMsgId";
        redisTemplate.opsForValue().set(roomKey, "0");
        redisTemplate.opsForSet().add("room:" + roomId + ":joinedUser",
                Arrays.stream(userIds).map(String::valueOf).toArray(String[]::new));
        for (Long uid : userIds) {
            String hashKey = "user:" + uid + ":roomAndLastMsgId";
            redisTemplate.opsForHash().put(hashKey, String.valueOf(roomId), "0");
        }
    }

    @AfterAll
    static void afterAll() {
        jwtUtilMockedStatic.close();
    }

    @AfterEach
    void tearDown() {
        messageRepositoryService.deleteAll(1L);
        messageRepositoryService.deleteAll(2L);
        messageRepositoryService.deleteAll(3L);
        userChatRoomRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        redisTemplate.getConnectionFactory().getConnection().flushAll();

    }
}