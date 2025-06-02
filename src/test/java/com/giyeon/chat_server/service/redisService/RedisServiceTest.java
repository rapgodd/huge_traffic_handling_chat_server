package com.giyeon.chat_server.service.redisService;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.entity.main.ChatRoom;
import com.giyeon.chat_server.entity.main.Role;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.entity.main.UserChatRoom;
import com.giyeon.chat_server.repository.main.RoomRepository;
import com.giyeon.chat_server.repository.main.UserChatRoomRepository;
import com.giyeon.chat_server.repository.main.UserRepository;
import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import com.giyeon.chat_server.service.redisService.repositoryService.MessageRepositoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MainRepositoryService mainRepositoryService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserChatRoomRepository userChatRoomRepository;

    @Autowired
    private MessageRepositoryService messageRepositoryService;

    @Autowired
    private IdGenerator idGenerator;

    @Test
    void getCurrentJoinedUsersTest_no_memory_data(){

        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        //채팅방에 참여하고 있는 유저 Id를 리스트로 따로 만들어 놓음
        List<Long> userIdList = List.of(1L,2L);

        //when
        List<Long> currentJoinedUsers = redisService.getCurrentJoinedUsers(room.getId());
        Set<Object> members = redisTemplate.opsForSet().members("room:" + room.getId() + ":joinedUser");

        //then
        //loop 돌면서 1L이랑 2L이 리스트에 있는지 확인
        for(Long userId : userIdList){
            assertThat(currentJoinedUsers).contains(userId);
        }

        //redis에도 적혀져 있는지 확인
        assertThat(members).size().isEqualTo(2);

        for(Object member : members){
            assertThat((String) member).isIn("1", "2");
        }

    }

    @Test
    void getCurrentJoinedUsersTest_with_memory_data(){

        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        //채팅방에 참여하고 있는 유저 Id를 리스트로 따로 만들어 놓음
        List<Long> userIdList = List.of(1L,2L);
        redisTemplate.opsForSet().add("room:" + room.getId() + ":joinedUser","1");
        redisTemplate.opsForSet().add("room:" + room.getId() + ":joinedUser","2");

        //when
        List<Long> currentJoinedUsers = redisService.getCurrentJoinedUsers(room.getId());
        Set<Object> members = redisTemplate.opsForSet().members("room:" + room.getId() + ":joinedUser");

        //then
        //loop 돌면서 1L이랑 2L이 리스트에 있는지 확인
        for(Long userId : userIdList){
            assertThat(currentJoinedUsers).contains(userId);
        }

        //redis에도 적혀져 있는지 확인
        assertThat(members).size().isEqualTo(2);

        for(Object member : members){
            assertThat((String) member).isIn("1", "2");
        }


    }

    @Test
    void addCurrentJoinedUserTest_no_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers_no_joined(1L, user1, user2);
        Long roomId = room.getId();

        String key = "room:" + roomId + ":joinedUser";

        //when
        redisService.addCurrentJoinedUser(1L, 1L);
        UserChatRoom user1ChatRoom = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(1L, 1L);

        //then
        assertThat(user1ChatRoom.getIsJoined()).isTrue();
        assertThat(redisTemplate.opsForSet().members(key).size()).isEqualTo(1);

    }


    @Test
    void addCurrentJoinedUserTest_with_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers_no_joined(1L, user1, user2);
        Long roomId = room.getId();

        String key = "room:" + roomId + ":joinedUser";
        redisTemplate.opsForSet().add(key, "1");

        //when
        redisService.addCurrentJoinedUser(2L, 1L);
        UserChatRoom user2ChatRoom = userChatRoomRepository.findUserChatRoomByUserIdAndRoomId(2L, 1L);

        //then
        assertThat(user2ChatRoom.getIsJoined()).isTrue();
        assertThat(redisTemplate.opsForSet().members(key).size()).isEqualTo(2);

    }

    @Test
    void putUserLastMsgIdInRoom_no_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";

        //when
        redisService.putUserLastMsgIdInRoom(1L, roomId, "100");

        //then
        assertThat(redisTemplate.opsForHash().get(key, roomId.toString())).isEqualTo("100");
    }

    @Test
    void putUserLastMsgIdInRoom_with_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";
        redisTemplate.opsForHash().put(key, roomId.toString(), "50");

        //when
        redisService.putUserLastMsgIdInRoom(1L, roomId, "100");

        //then
        assertThat(redisTemplate.opsForHash().get(key, roomId.toString())).isEqualTo("100");
        assertThat(mainRepositoryService.getLastMsgIdInUserChatRoom(user1.getId(), roomId)).isEqualTo(100L);
    }

    @Test
    void putUsersLastMsgIdInRoom_with_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";
        String key2 = "user:" + user2.getId() + ":roomAndLastMsgId";
        redisTemplate.opsForHash().put(key, roomId.toString(), "50");

        //when
        redisService.putUsersLastMsgIdInRoom(List.of(user1.getId(), user2.getId()), roomId, "100");

        //then
        assertThat(redisTemplate.opsForHash().get(key, roomId.toString())).isEqualTo("100");
        assertThat(redisTemplate.opsForHash().get(key2, roomId.toString())).isEqualTo("100");
        assertThat(mainRepositoryService.getLastMsgIdInUserChatRoom(user1.getId(), roomId)).isEqualTo(100L);
        assertThat(mainRepositoryService.getLastMsgIdInUserChatRoom(user2.getId(), roomId)).isEqualTo(100L);
    }

    @Test
    void putUsersLastMsgIdInRoom_no_memory_data(){
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";
        String key2 = "user:" + user2.getId() + ":roomAndLastMsgId";

        //when
        redisService.putUsersLastMsgIdInRoom(List.of(user1.getId(), user2.getId()), roomId, "100");

        //then
        assertThat(redisTemplate.opsForHash().get(key, roomId.toString())).isEqualTo("100");
        assertThat(redisTemplate.opsForHash().get(key2, roomId.toString())).isEqualTo("100");
        assertThat(mainRepositoryService.getLastMsgIdInUserChatRoom(user1.getId(), roomId)).isEqualTo(100L);
        assertThat(mainRepositoryService.getLastMsgIdInUserChatRoom(user2.getId(), roomId)).isEqualTo(100L);

    }

    @Test
    void getUserLastReadMsgIdInRoom_no_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";

        //when
        Long lastMsgId = redisService.getUserLastReadMsgIdInRoom(user1.getId(), roomId);

        //then
        assertThat(lastMsgId).isEqualTo(0L);
        assertThat(redisTemplate.opsForHash().get(key, roomId.toString())).isEqualTo("0");
    }

    @Test
    void getLastMsgIdInRoom_no_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":lastMsgId";

        //when
        Long lastMsgId = redisService.getLastMsgIdInRoom(roomId);

        //then
        assertThat(lastMsgId).isEqualTo(0L);
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("0");
    }

    @Test
    void getUserLastReadMsgIdInRoom_with_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "user:" + user1.getId() + ":roomAndLastMsgId";
        redisTemplate.opsForHash().put(key, roomId.toString(), "100");

        //when
        Long lastMsgId = redisService.getUserLastReadMsgIdInRoom(user1.getId(), roomId);

        //then
        assertThat(lastMsgId).isEqualTo(100L);
    }

    @Test
    void getLastMsgIdInRoom_with_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":lastMsgId";
        redisTemplate.opsForValue().set(key, "100");

        //when
        Long lastMsgId = redisService.getLastMsgIdInRoom(roomId);

        //then
        assertThat(lastMsgId).isEqualTo(100L);
    }

    @Test
    void putLastMsgIdInRoom_no_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":lastMsgId";

        //when
        redisService.putLastMsgIdInRoom(roomId, "100");

        //then
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("100");
        assertThat(mainRepositoryService.getLastMsgIdInRoom(roomId)).isEqualTo(100L);
    }

    @Test
    void putLastMsgIdInRoom_with_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":lastMsgId";
        redisTemplate.opsForValue().set(key, "50");

        //when
        redisService.putLastMsgIdInRoom(roomId, "100");

        //then
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("100");
        assertThat(mainRepositoryService.getLastMsgIdInRoom(roomId)).isEqualTo(100L);
    }

    @Test
    void removeCurrentJoinedUser_no_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":joinedUser";

        //when
        redisService.removeJoinedUserInRoom(1L, 1L);

        //then
        assertThat(redisTemplate.opsForSet().members(key)).isEmpty();
        assertThat(redisTemplate.opsForSet().members(key).size()).isEqualTo(0);
    }

    @Test
    void removeCurrentJoinedUser_with_memory_data() {
        //given
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        ChatRoom room = createRoomWithUsers(1L, user1, user2);
        Long roomId = room.getId();
        String key = "room:" + roomId + ":joinedUser";
        redisTemplate.opsForSet().add(key, "1");
        redisTemplate.opsForSet().add(key, "2");

        //when
        redisService.removeJoinedUserInRoom(1L, 1L);

        //then
        assertThat(redisTemplate.opsForSet().members(key).size()).isEqualTo(1);
        assertThat(redisTemplate.opsForSet().members(key)).contains("2");
        assertThat(redisTemplate.opsForSet().members(key)).doesNotContain("1");
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


    private ChatRoom createRoomWithUsers_no_joined(Long roomId, User... users) {
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
                    .isJoined(false)
                    .leavedAt(null)
                    .lastReadMessageId(0L)
                    .build();
            userChatRoomRepository.save(ucr);
        }
        return room;
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