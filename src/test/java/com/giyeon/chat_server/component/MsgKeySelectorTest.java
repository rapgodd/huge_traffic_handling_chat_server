package com.giyeon.chat_server.component;

import com.github.snksoft.crc.CRC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MsgKeySelectorTest {

    @Test
    void getDbKey() {
        //given
        MsgKeySelector messageKeySelector = new MsgKeySelector();
        messageKeySelector.getShardList().add("shard1");
        messageKeySelector.getShardList().add("shard2");
        messageKeySelector.getShardList().add("shard3");
        long id1 = 1000L; // 1
        long id2 = 1001L; // 2
        long id3 = 1002L; // 0

        //when
        String dbKey1 = messageKeySelector.getDbKey(id1);
        String dbKey2 = messageKeySelector.getDbKey(id2);
        String dbKey3 = messageKeySelector.getDbKey(id3);

        //then
        Assertions.assertEquals(dbKey1, messageKeySelector.getShardList().get(1));
        Assertions.assertEquals(dbKey2, messageKeySelector.getShardList().get(2));
        Assertions.assertEquals(dbKey3, messageKeySelector.getShardList().get(0));

    }

}