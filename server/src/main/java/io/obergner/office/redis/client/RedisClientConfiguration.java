package io.obergner.office.redis.client;

import io.obergner.office.redis.RedisServerSettings;
import io.obergner.office.redis.server.RedisServerDevelopmentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RedisServerSettings redisServerSettings;

    @Bean
    @DependsOn("redisServer")
    public JedisPool jedisPool() {
        return new JedisPool(new JedisPoolConfig(), this.redisServerSettings.getHost(), this.redisServerSettings.getPort());
    }
}
