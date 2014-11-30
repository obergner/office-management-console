package io.obergner.office.redis.server;

import io.obergner.office.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile(Profiles.PRODUCTION)
@Configuration
public class RedisServerProductionConfiguration implements RedisServerConfiguration {

    @Bean
    public EmbeddedDevelopmentRedisServer redisServer() throws IOException {
        return null;
    }
}
