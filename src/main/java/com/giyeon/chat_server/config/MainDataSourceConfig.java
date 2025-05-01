package com.giyeon.chat_server.config;

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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

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
    @Bean
    public DataSource mainDataSource() {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(dataSourceProperty.getMainDb().getUrl());
        hc.setUsername(dataSourceProperty.getUsername());
        hc.setPassword(dataSourceProperty.getPassword());
        hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hc.setMaximumPoolSize(20);
        hc.setMinimumIdle(20);
        return new HikariDataSource(hc);
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
