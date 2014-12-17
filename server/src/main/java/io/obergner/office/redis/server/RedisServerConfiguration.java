package io.obergner.office.redis.server;

import io.obergner.office.redis.RedisServerSettings;

public interface RedisServerConfiguration {

    EmbeddedDevelopmentRedisServer redisServer(RedisServerSettings redisServerSettings) throws Exception;
}
