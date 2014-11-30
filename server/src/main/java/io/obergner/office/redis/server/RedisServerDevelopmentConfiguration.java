package io.obergner.office.redis.server;

import io.obergner.office.Profiles;
import io.obergner.office.redis.RedisServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Profile(Profiles.DEVELOPMENT)
@Configuration
@EnableConfigurationProperties(RedisServerSettings.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedisServerDevelopmentConfiguration implements RedisServerConfiguration {

    @Autowired
    private RedisServerSettings redisServerSettings;

    @Bean
    public EmbeddedDevelopmentRedisServer redisServer() throws IOException {
        return new EmbeddedDevelopmentRedisServer(this.redisServerSettings.getPort());
    }
}
