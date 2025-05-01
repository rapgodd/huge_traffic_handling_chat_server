package com.giyeon.chat_server.component;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterSlaveKeyRouter {
    @Getter
    @Setter
    private String masterKey = "";
    private final List<String> slaveKeys = new ArrayList<>();

    private final AtomicInteger counter = new AtomicInteger(0);
    public void addSlaveKey(String key){
        slaveKeys.add(key);
    }

    public String getSlaveKey(){
        //update --> return the update value
        int current = counter.updateAndGet(i -> i >= slaveKeys.size() - 1 ? 0 : i + 1);
        return slaveKeys.get(current);
    }


}
