package io.obergner.office.redis.server;

public interface RedisServerConfiguration {

    EmbeddedDevelopmentRedisServer redisServer() throws Exception;
}
