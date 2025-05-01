package com.giyeon.chat_server.config;

import com.giyeon.chat_server.aspect.ShardAspect;
import com.giyeon.chat_server.component.KeySelector;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private final KeySelector keySelector;

    public RoutingDataSource(Map<Object,Object> dataSources, KeySelector keySelector){
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
