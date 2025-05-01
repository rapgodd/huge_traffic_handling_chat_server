package com.giyeon.chat_server.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix="spring.datasource")
@Getter
@ToString
@AllArgsConstructor
public class DataSourceProperty {

    private String username;
    private String password;
    private List<DataSourceDto> shardList;
    private DataSourceDto mainDb;
    private List<DataSourceDto> slaves;

    @AllArgsConstructor
    @Getter
    @ToString
    public static class DataSourceDto{
        private String key;
        private String url;
    }


}
