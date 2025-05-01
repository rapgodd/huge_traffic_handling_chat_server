package com.giyeon.chat_server.config.main;

import com.giyeon.chat_server.component.MasterSlaveKeyRouter;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

public class MainRoutingDataSource extends AbstractRoutingDataSource {

     private final MasterSlaveKeyRouter masterSlaveKeyRouter;

    public MainRoutingDataSource(Map<Object, Object> mainDataSourceMap,MasterSlaveKeyRouter masterSlaveKeyRouter) {
        this.masterSlaveKeyRouter = masterSlaveKeyRouter;
        super.setTargetDataSources(mainDataSourceMap);
        this.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {

        if(TransactionSynchronizationManager.isCurrentTransactionReadOnly()){
            return masterSlaveKeyRouter.getSlaveKey();
        }else{
            return masterSlaveKeyRouter.getMasterKey();
        }

    }
}
