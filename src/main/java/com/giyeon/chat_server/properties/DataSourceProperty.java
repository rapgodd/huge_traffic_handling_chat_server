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
    private List<Shard> shardList;
    private Main mainDb;


    @AllArgsConstructor
    @Getter
    @ToString
    public static class Shard{

        private String key;
        private String url;

    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Main{

        private String key;
        private String url;

    }


}
