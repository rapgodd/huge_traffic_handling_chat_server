package com.giyeon.chat_server.config;

import com.giyeon.chat_server.component.KeySelector;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfiguration {

    private final DataSourceProperty dataSourceProperty;

    public DataSource createDataSource(String url, String username, String password){
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(url);
        hc.setUsername(username);
        hc.setPassword(password);
        hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hc.setMaximumPoolSize(20);
        hc.setMinimumIdle(20);
        return new HikariDataSource(hc);
    }

    @Bean
    public DataSource dataSource(){
        Map<Object, Object> dataSourcesMap = new HashMap<>();
        KeySelector keySelector = new KeySelector();

        dataSourceProperty.getShardList().forEach(shard -> {
            DataSource dataSource = createDataSource(shard.getUrl(),
                    dataSourceProperty.getUsername(),
                    dataSourceProperty.getPassword());

            dataSourcesMap.put(shard.getKey(),dataSource);

            keySelector.getShardList().add(shard.getKey());
        });

        return new LazyConnectionDataSourceProxy(new RoutingDataSource(dataSourcesMap,keySelector));
    }



}
