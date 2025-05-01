package com.giyeon.chat_server.config.main;

import com.giyeon.chat_server.component.MasterSlaveKeyRouter;
import com.giyeon.chat_server.properties.DataSourceProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableJpaRepositories(
        basePackages = "com.giyeon.chat_server.repository.main",
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
@Configuration
@RequiredArgsConstructor
public class MainDataSourceConfig {

    private final DataSourceProperty dataSourceProperty;

    @Primary
    public DataSource createDataSource(String url, String username, String password) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(url);
        hc.setUsername(username);
        hc.setPassword(password);
        hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hc.setMaximumPoolSize(20);
        hc.setMinimumIdle(20);
        return new HikariDataSource(hc);
    }

    @Primary
    @Bean
    public DataSource mainDataSource(){

        Map<Object, Object> mainDataSourceMap = new HashMap<>();
        MasterSlaveKeyRouter masterSlaveKeyRouter = new MasterSlaveKeyRouter();

        //master
        DataSource master = createDataSource(dataSourceProperty.getMainDb().getUrl(),
                                dataSourceProperty.getUsername(),
                                dataSourceProperty.getPassword());

        masterSlaveKeyRouter.setMasterKey(dataSourceProperty.getMainDb().getKey());
        mainDataSourceMap.put(dataSourceProperty.getMainDb().getKey(),master);


        //slave
        dataSourceProperty.getSlaves().forEach(slave -> {
            DataSource slaveDataSource = createDataSource(slave.getUrl(),
                    dataSourceProperty.getUsername(),
                    dataSourceProperty.getPassword());

            masterSlaveKeyRouter.addSlaveKey(slave.getKey());
            mainDataSourceMap.put(slave.getKey(),slaveDataSource);
        });

        return new LazyConnectionDataSourceProxy(new MainRoutingDataSource(mainDataSourceMap,masterSlaveKeyRouter));




    }


    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mainDataSource())
                .packages("com.giyeon.chat_server.entity.main")
                .persistenceUnit("main")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager mainTransactionManager(@Qualifier("mainEntityManagerFactory") EntityManagerFactory emFactoryBean) {
        return new JpaTransactionManager(emFactoryBean);
    }

}
