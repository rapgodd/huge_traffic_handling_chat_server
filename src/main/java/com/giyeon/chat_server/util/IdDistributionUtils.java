package com.giyeon.chat_server.util;

import com.giyeon.chat_server.ws.SessionRegistry;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class IdDistributionUtils {

    public static void distributeRemoteAndLocal(List<Long> userIds, List<Long> localSessionUsersList,
                                                List<Long> remoteSessionUsersList, SessionRegistry sessionRegistry,
                                                RedisTemplate<String, Object> redisTemplate) {
        for (Long userId : userIds) {

            if (sessionRegistry.isUserSessionExist(userId)) {

                localSessionUsersList.add(userId);

            } else {
                String IP = (String) redisTemplate.opsForValue().get(String.valueOf(userId));

                if(IP!=null){
                    remoteSessionUsersList.add(userId);
                }

            }
        }
    }

}
