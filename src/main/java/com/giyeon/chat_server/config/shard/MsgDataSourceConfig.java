package com.giyeon.chat_server.config.shard;

import com.giyeon.chat_server.component.MsgKeySelector;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableJpaRepositories(
        basePackages = "com.giyeon.chat_server.repository.message",
        entityManagerFactoryRef = "messageEntityManagerFactory",
        transactionManagerRef = "messagePlatformTransactionManager"
)
@Configuration
@RequiredArgsConstructor
public class MsgDataSourceConfig {

    private final DataSourceProperty dataSourceProperty;

    public DataSource createDataSource(String url, String username, String password){
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(url);
        hc.setUsername(username);
        hc.setPassword(password);
        hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hc.setMaximumPoolSize(3);
        hc.setMinimumIdle(3);
        return new HikariDataSource(hc);
    }

    @Bean
    public DataSource messageDataSource(){
        Map<Object, Object> dataSourcesMap = new HashMap<>();
        MsgKeySelector keySelector = new MsgKeySelector();

        dataSourceProperty.getShardList().forEach(shard -> {
            DataSource dataSource = createDataSource(shard.getUrl(),
                    dataSourceProperty.getUsername(),
                    dataSourceProperty.getPassword());

            dataSourcesMap.put(shard.getKey(),dataSource);

            keySelector.getShardList().add(shard.getKey());
        });

        return new LazyConnectionDataSourceProxy(new MsgRoutingDataSource(dataSourcesMap,keySelector));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean messageEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(messageDataSource())
                .packages("com.giyeon.chat_server.entity.message")
                .persistenceUnit("message")
                .build();
    }

    @Bean
    public PlatformTransactionManager messagePlatformTransactionManager(@Qualifier("messageEntityManagerFactory") EntityManagerFactory emFactoryBean) {
        return new JpaTransactionManager(emFactoryBean);
    }
}
