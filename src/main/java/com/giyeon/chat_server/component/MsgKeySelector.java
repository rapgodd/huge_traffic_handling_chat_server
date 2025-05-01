package com.giyeon.chat_server.component;


import com.github.snksoft.crc.CRC;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MsgKeySelector {

    @Getter
    private final List<String> shardList = new ArrayList<>();

    public String getDbKey(Long id){
        if (id == null) {
            // 초기화 시점이나 id 미설정 시 기본 샤드 사용
            return "my-shard-1";
        }

        byte[] bytes = String.valueOf(id).getBytes(StandardCharsets.UTF_8);
        int key = (int) CRC.calculateCRC(CRC.Parameters.CCITT, bytes) & 0x3FFF; //key -> String -> byte -> crc16
        return getShardKey(key);
    }

    private String getShardKey(long key) {
        // 0~5460 , 5461~10921 , 10922~16383
        if(0<=key&&key<=5460){
            System.out.println("0번째 샤드가 사용됩니다.");
            return shardList.get(0);
        }else if(5460<=key&&key<=10921){
            System.out.println("1번째 샤드가 사용됩니다");
            return shardList.get(1);
        }else{
            System.out.println("2번째 샤드가 사용됩니다.");
            return shardList.get(2);
        }

    }
}
