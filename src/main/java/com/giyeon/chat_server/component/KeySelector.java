package com.giyeon.chat_server.component;


import com.github.snksoft.crc.CRC;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KeySelector {

    @Getter
    private final List<String> shardList = new ArrayList<>();

    public String getDbKey(Long id){
        if (id == null) {
            return "my-shard-1";
        }

        byte[] bytes = String.valueOf(id).getBytes(StandardCharsets.UTF_8);
        int key = (int) CRC.calculateCRC(CRC.Parameters.CCITT, bytes) & 0x3FFF;
        return getShardKey(key);
    }

    private String getShardKey(long key) {
        if(0<=key&&key<=5460){
            return shardList.get(0);
        }else if(5460<=key&&key<=10921){
            return shardList.get(1);
        }else{
            return shardList.get(2);
        }

    }
}
