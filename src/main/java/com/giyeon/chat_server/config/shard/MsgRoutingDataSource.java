package com.giyeon.chat_server.config.shard;


import com.giyeon.chat_server.aspect.ShardAspect;
import com.giyeon.chat_server.component.MsgKeySelector;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

public class MsgRoutingDataSource extends AbstractRoutingDataSource {

    private final MsgKeySelector keySelector;

    public MsgRoutingDataSource(Map<Object,Object> dataSources, MsgKeySelector keySelector){
        super.setTargetDataSources(dataSources);
        this.keySelector = keySelector;
        this.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        Long currentId = ShardAspect.getCurrentId();
        return keySelector.getDbKey(currentId);
    }
}
