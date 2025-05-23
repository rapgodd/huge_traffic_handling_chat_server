package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.RoomCreateDto;
import com.giyeon.chat_server.dto.RoomInfoDto;
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
import com.giyeon.chat_server.service.msgSender.JoinMsgSenderService;
import com.giyeon.chat_server.service.msgSender.threadPoolSender.ThreadSendingService;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import com.giyeon.chat_server.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final Logger log = LoggerFactory.getLogger(RoomServiceTest.class);
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
    @Autowired
    private MainRepositoryService mainRepositoryService;
    @MockitoBean
    private JoinMsgSenderService joinMsgSenderService;


    @BeforeAll
    static void beforeAll() {
        jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        jwtUtilMockedStatic.when(() -> JwtUtil.getUserId(anyString())).thenReturn(1L);
    }

    @Test
    @Tag("joinRoom")
    void joinRoom() throws InterruptedException {

        //given
        BDDMockito.doNothing().when(joinMsgSenderService).sendJoinMsgToLocal(anyList(), anyLong(), anyLong());
        // 유저 생성
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");
        // 방 생성
        ChatRoom roomWithUsersIsJoinedFalse = createRoomWithUsers_isJoined_false(1L, user1, user2);
        Long roomId = roomWithUsersIsJoinedFalse.getId();

        //when
        //방 입장
        roomService.joinRoom(roomId);
        String key = "room:" + roomId + ":joinedUser";
        String lastMsgKey = "user:" + user1.getId() + ":roomAndLastMsgId";
        String roomLastMsgIdKey = "room:1:lastMsgId";
        UserChatRoom room = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(user1.getId(), roomId);

        //then
        //redis에 제대로 방에 입장한 유저 목록에 1있어야 함
        assertThat(redisTemplate.opsForSet().members(key)).allMatch((a) -> {
            return a.equals("1");
        });
        //redis 유저의 방 마지막 메세지가 0L로 되어 있어야 함
        assertThat((String)redisTemplate.opsForHash().get(lastMsgKey, String.valueOf(roomId))).isEqualTo(String.valueOf(0));
        //Redis의 방 마지막 메세지가 설정되어 있어야 함
        assertThat((String) redisTemplate.opsForValue().get(roomLastMsgIdKey)).isEqualTo("0");
        //db에 userChatRoom.getIsJoined()값이 true이어야 함
        assertThat(room.getIsJoined()).isTrue();
        //db userChatRoom.getLastReadMessageId가 업데이트 되었는지 확인
        assertThat(room.getLastReadMessageId()).isEqualTo(0L);
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
                assertFalse(result.get(i).isMe());
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

    @Test
    @Tag("closeRoom")
    void closeRoom(){

        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);

        redisTemplate.opsForSet().add("room:" + room.getId() + ":joinedUser", String.valueOf(1L), String.valueOf(2L));

        //when
        roomService.closeRoom(room.getId());
        int size = redisTemplate.opsForSet().members("room:" + room.getId() + ":joinedUser").size();
        UserChatRoom closedUc = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, room.getId());

        //then
        Assertions.assertTrue(closedUc.getLeavedAt() == null);
        Assertions.assertTrue(!closedUc.getIsJoined());
        Assertions.assertTrue(closedUc.getChatRoom().getId().equals(room.getId()));
        Assertions.assertTrue(closedUc.getUser().getId() == 1L);
        Assertions.assertEquals(size, 1);




    }

    @Test
    @Tag("getUserRooms")
    void getUserRooms() {
        // given
        // 1. 사용자 6명 생성
        User u1 = createUser(1L, "user1");
        User u2 = createUser(2L, "user2");
        User u3 = createUser(3L, "user3");
        User u4 = createUser(4L, "user4");
        User u5 = createUser(5L, "user5");
        User u6 = createUser(6L, "user6");

        // 2. 방 5개 생성 및 유저 매핑
        ChatRoom r1 = createRoomWithUsers(1L, u1, u2);
        ChatRoom r2 = createRoomWithUsers(2L, u1, u3);
        ChatRoom r3 = createRoomWithUsers(3L, u1, u4);
        ChatRoom r4 = createRoomWithUsers(4L, u1, u5);
        ChatRoom r5 = createRoomWithUsers(5L, u1, u2, u3, u4, u5, u6);

        // 3. 각 방에 메시지 5개씩
        RoomData r1Data = insertMessages(r1, 2L, 5);
        RoomData r2Data = insertMessages(r2, 3L, 5);
        Room2Data r3Data = insertMessagesReturnRoom2Data(r3, 4L, 5);
        Room2Data r4Data = insertMessagesReturnRoom2Data(r4, 1L, 5);
        RoomData r5Data = insertMessages(r5, 2L, 5);
        // userChatROom에 마지막 메시지 id 업데이트
        updateUcrLastMsg(r1, r1Data);
        updateUcrLastMsg(r2, r2Data);
        updateUcrLastMsgRoom2Data(r3, r3Data);
        updateUcrLastMsgRoom2Data(r4, r4Data);
        updateUcrLastMsg(r5, r5Data);

        // when
        Pageable pageable = PageRequest.of(0, 50);
        List<RoomInfoDto> result = roomService.getUserRooms(pageable);

        // then
        // result의 lastMessageTime이 내림차순으로 정렬되었는지 확인
        for (int i = 0; i < result.size() - 1; i++) {
            ZonedDateTime current = result.get(i).getLastMessageTime();
            ZonedDateTime next    = result.get(i + 1).getLastMessageTime();
            assertFalse(current.isBefore(next));
        }

        // 안읽은 메세지 수 제대로 계산되었는지 확인
        for (RoomInfoDto roomInfoDto : result) {
            if(roomInfoDto.getRoomId()==3L||roomInfoDto.getRoomId()==4L){
                assertThat(roomInfoDto.getUnreadCount()).isEqualTo(3);    
            }
        }

        assertThat(result)
                .extracting(RoomInfoDto::getRoomImageUrl)
                .allMatch(url -> url != null);

        assertThat(result)
                .extracting(RoomInfoDto::getRoomId)
                .allMatch(id -> id != null);

        // joinedUserCount 내림차순 정렬 후 검증
        result.sort((a, b) -> Integer.compare(b.getJoinedUserCount(), a.getJoinedUserCount()));
        assertThat(result.get(0).getJoinedUserCount()).isEqualTo(6);
        assertThat(result.get(1).getJoinedUserCount()).isEqualTo(2);

        // r1 최종 메시지 확인
        RoomInfoDto dto1 = result.stream()
                .filter(dto -> dto.getRoomId().equals(r1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(dto1.getLastMessage()).isEqualTo(r1Data.lastMessage);
        LocalDateTime lastMessageTime = dto1.getLastMessageTime().toLocalDateTime();
        LocalDateTime r1LastMsgTime = r1Data.lastMessageTime.toLocalDateTime();
        assertThat(lastMessageTime).isEqualTo(r1LastMsgTime);
    }

    private void updateUcrLastMsgRoom2Data(ChatRoom r, Room2Data r2Data) {
        UserChatRoom ucr = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, r.getId());
        ucr.updateNewMsgId(r2Data.secMessageId);
        userChatRoomRepository.save(ucr);
    }

    private void updateUcrLastMsg(ChatRoom r, RoomData rData) {
        UserChatRoom ucr = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, r.getId());
        ucr.updateNewMsgId(rData.lastMessageId);
        userChatRoomRepository.save(ucr);
    }


    @Test
    void getOnlyJoinedUserRooms() {

        // given
        User u1 = createUser(1L, "user1");
        User u2 = createUser(2L, "user2");
        ChatRoom r1 = createRoomWithUsers(1L, u1, u2);
        insertMessages(r1, 2L, 5);
        UserChatRoom ucr1 = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, r1.getId());
        ucr1.updateLeavedAt(ZonedDateTime.now());
        userChatRoomRepository.save(ucr1);
        Pageable pageable = PageRequest.of(0, 50);

        // when
        List<RoomInfoDto> result = roomService.getUserRooms(pageable);

        // then
        assertThat(result.size()).isEqualTo(0);
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

    private RoomData insertMessages(ChatRoom room, Long senderId, int count) {
        String lastMsg = null;
        ZonedDateTime lastTime = null;
        Long lastMsgId = 0L;

        long roomOffset = room.getId() * (count + 1L);

        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"))
                .truncatedTo(ChronoUnit.SECONDS)
                .plusSeconds(roomOffset);

        for (int i = 1; i <= count; i++) {
            ZonedDateTime ts = base.plusSeconds(i);
            Message msg = Message.builder()
                    .id(idGenerator.nextId())
                    .roomId(room.getId())
                    .senderId(senderId)
                    .message(room.getId() + " msg" + i)
                    .createdAt(ts)
                    .build();
            messageService.save(room.getId(), msg);

            if (i == count) {
                lastMsg    = msg.getMessage();
                lastTime   = ts;
                lastMsgId  = msg.getId();
            }
        }
        return new RoomData(lastMsg, lastTime, lastMsgId);
    }

    private Room2Data insertMessagesReturnRoom2Data(ChatRoom room, Long senderId, int count) {
        String secMsg = null;
        ZonedDateTime secTime = null;
        Long secId = 0L;

        long roomOffset = room.getId() * (count + 1L);

        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"))
                .truncatedTo(ChronoUnit.SECONDS)
                .plusSeconds(roomOffset);

        for (int i = 1; i <= count; i++) {
            ZonedDateTime ts = base.plusSeconds(i);
            Message msg = Message.builder()
                    .id(idGenerator.nextId())
                    .roomId(room.getId())
                    .senderId(senderId)
                    .message(room.getId() + " msg" + i)
                    .createdAt(ts)
                    .build();
            messageService.save(room.getId(), msg);

            if (i == 2) {
                secMsg    = msg.getMessage();
                secTime   = ts;
                secId  = msg.getId();
            }
        }
        return new Room2Data(secMsg, secTime, secId);
    }

    private static class RoomData {
        final String lastMessage;
        final ZonedDateTime lastMessageTime;
        final Long lastMessageId;

        RoomData(String msg, ZonedDateTime time, Long lastMessageId) {
            this.lastMessage = msg;
            this.lastMessageTime = time;
            this.lastMessageId = lastMessageId;
        }
    }

    private static class Room2Data {
        final String secMessage;
        final ZonedDateTime secMessageTime;
        final Long secMessageId;

        Room2Data(String secMsg, ZonedDateTime secTime, Long secMessageId) {
            this.secMessage = secMsg;
            this.secMessageTime = secTime;
            this.secMessageId = secMessageId;
        }
    }

    @AfterEach
    void tearDown() {
        messageService.deleteAll(1L);
        messageService.deleteAll(2L);
        messageService.deleteAll(3L);
        userChatRoomRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        redisTemplate.delete("user:1:roomAndLastMsgId");
        redisTemplate.delete("user:2:roomAndLastMsgId");
        redisTemplate.delete("room:1:joinedUser");
        redisTemplate.delete("room:1:lastMsgId");

    }
    @AfterAll
    static void afterAll() {
        jwtUtilMockedStatic.close();
    }

}