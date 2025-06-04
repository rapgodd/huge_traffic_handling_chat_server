package com.giyeon.chat_server.service.redisService;

import com.giyeon.chat_server.service.redisService.repositoryService.MainRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MainRepositoryService mainRepositoryService;

    /**
     * Write Through
     */
    public void putUserLastMsgIdInRoom(Long userId, Long chatRoomId, String roomLastMsgId) {

        String key = "user:" + userId + ":roomAndLastMsgId";

        boolean isNewKey = redisTemplate.hasKey(key)?false:true;

        redisTemplate.opsForHash().put(key, chatRoomId.toString(), roomLastMsgId);
        mainRepositoryService.updateUserChatRoomNewMsgId(chatRoomId, userId, Long.valueOf(roomLastMsgId));

        if (isNewKey) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }

    }


    public void putUsersLastMsgIdInRoom(List<Long> userIds, Long chatRoomId, String roomLastMsgId) {

        for (Long userId : userIds) {
            String key = "user:" + userId + ":roomAndLastMsgId";

            boolean isNewKey = redisTemplate.hasKey(key)?false:true;

            redisTemplate.opsForHash().put(key, chatRoomId.toString(), roomLastMsgId);

            if (isNewKey) {
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }
        }
        //batch update
        mainRepositoryService.batchUpdateUserChatRoomsNewMsgId(chatRoomId, userIds, Long.valueOf(roomLastMsgId));

    }

    /**
     * Lazy loading
     */
    public Long getLastMsgIdInRoom(Long roomId) {
        String key = "room:" + roomId + ":lastMsgId";
        Object raw = redisTemplate.opsForValue().get(key);

        if (raw != null) {
            return Long.valueOf((String) raw);
        }

        Long lastMsgIdInRoom = mainRepositoryService.getLastMsgIdInRoom(roomId);


        redisTemplate.opsForValue().set(key, lastMsgIdInRoom.toString(), 1, TimeUnit.HOURS);
        return lastMsgIdInRoom;

    }

    /**
     * Write Through
     */
    public void putLastMsgIdInRoom(Long roomId, String msgId) {
        String key = "room:" + roomId + ":lastMsgId";
        redisTemplate.opsForValue().set(key, msgId, 1, TimeUnit.HOURS);
        mainRepositoryService.updateRoomLastMsgId(roomId, Long.valueOf(msgId));
    }

    /**
     * Lazy loading
     */
    public Long getUserLastReadMsgIdInRoom(Long userId, Long roomId) {
        String key = "user:" + userId + ":roomAndLastMsgId";
        boolean isNewKey = redisTemplate.hasKey(key)?false:true;
        Object raw = redisTemplate.opsForHash().get(key, roomId.toString());

        if (raw != null) {
            return Long.valueOf((String) raw);
        }

        String lastMsgIdInUserChatRoom = String.valueOf(mainRepositoryService.getLastMsgIdInUserChatRoom(userId, roomId));
        redisTemplate.opsForHash().put(key,roomId.toString(),lastMsgIdInUserChatRoom);

        if (isNewKey) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }

        return Long.valueOf(lastMsgIdInUserChatRoom);
    }

    /**
     * Lazy loading
     */
    public List<Long> getCurrentJoinedUsers(Long roomId) {
        String key = "room:" + roomId + ":joinedUser";


        if (redisTemplate.hasKey(key)) {
            Set<Object> rawSet = redisTemplate.opsForSet().members(key);

            return rawSet.stream()
                    .map(o -> Long.valueOf((String)o))
                    .collect(Collectors.toList());
        }


        List<Long> joinedFromDb = mainRepositoryService.getJoinedUserChatRooms(roomId);
        if (!joinedFromDb.isEmpty()) {

            String[] members = joinedFromDb.stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);
            redisTemplate.opsForSet().add(key, (Object[]) members);
        }


        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        return joinedFromDb;

    }

    public void addCurrentJoinedUser(Long userId, Long roomId) {
        String key = "room:" + roomId + ":joinedUser";
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
        mainRepositoryService.updateUserChatRoomToJoined(roomId, userId);
    }


    public void removeJoinedUserInRoom(Long userId, Long roomId) {
        String key = "room:" + roomId + ":joinedUser";
        redisTemplate.opsForSet().remove(key, String.valueOf(userId));
    }

}
