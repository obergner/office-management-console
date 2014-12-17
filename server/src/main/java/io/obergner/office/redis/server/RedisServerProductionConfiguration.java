package io.obergner.office.redis.server;

import io.obergner.office.Profiles;
import io.obergner.office.redis.RedisServerSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile(Profiles.PRODUCTION)
@Configuration
public class RedisServerProductionConfiguration implements RedisServerConfiguration {

    @Bean
    public EmbeddedDevelopmentRedisServer redisServer(final RedisServerSettings redisServerSettings) throws IOException {
        return null;
    }
}
