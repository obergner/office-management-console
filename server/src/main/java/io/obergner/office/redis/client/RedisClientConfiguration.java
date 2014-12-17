package io.obergner.office.redis.client;

import io.obergner.office.redis.RedisServerSettings;
import io.obergner.office.redis.server.RedisServerDevelopmentConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Import(RedisServerDevelopmentConfiguration.class)
@EnableConfigurationProperties(RedisServerSettings.class)
public class RedisClientConfiguration {

    @Bean
    @DependsOn("redisServer")
    public JedisPool jedisPool(final RedisServerSettings redisServerSettings) {
        return new JedisPool(new JedisPoolConfig(), redisServerSettings.getHost(), redisServerSettings.getPort());
    }
}
