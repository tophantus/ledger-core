package com.example.ledgercore.common.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    private final RedissonProperties properties;

    @Bean
    public RedissonClient redissonClient(){

        Config config = new Config();

        config.useSingleServer()
                .setAddress(
                        properties.getAddress()
                );

        return Redisson.create(config);
    }

}