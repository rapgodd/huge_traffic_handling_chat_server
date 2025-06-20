package com.giyeon.chat_server.config;

import io.lettuce.core.ReadFrom;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Profile("!test")
@Configuration
@EnableCaching
public class ProdRedisConfig {

    @Bean
    public RedisConnectionFactory redisConn(RedisProperties redisProperties){
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
        clusterConfiguration.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        return new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer hashValueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(hashValueSerializer);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        return template;
    }

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties){
        Config config = new Config();
        if(redisProperties.getCluster() != null){
            for(String node : redisProperties.getCluster().getNodes()){
                config.useClusterServers().addNodeAddress("redis://" + node);
            }
        }else{
            config.useSingleServer()
                    .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        }

        return Redisson.create(config);
    }


}
