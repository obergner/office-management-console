package io.obergner.office.test;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * Created by obergner on 01.11.14.
 */
public class EmbeddedRedisServer extends ExternalResource {

    public static final EmbeddedRedisServer listenOnDefaultPort() {
        return listenOnPort(6379);
    }

    public static final EmbeddedRedisServer listenOnPort(final int port) {
        try {
            return new EmbeddedRedisServer(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RedisServer redisServer;

    private final Jedis redisClient;

    private EmbeddedRedisServer(final int port) throws IOException {
        this.redisServer = new RedisServer(port);
        this.redisClient = new Jedis("127.0.0.1", port);
    }

    public Jedis client() {
        return this.redisClient;
    }

    @Override
    protected void before() throws Throwable {
        this.log.info("Starting embedded RedisServer {} ...", this.redisServer);
        this.redisServer.start();
        this.log.info("Embedded RedisServer {} started", this.redisServer);
    }

    @Override
    protected void after() {
        try {
            this.log.info("Stopping RedisClient {} ...", this.redisClient);
            this.redisClient.shutdown();
            this.log.info("RedisClient {} stopped", this.redisClient);

            this.log.info("Stopping embedded RedisServer {} ...", this.redisServer);
            this.redisServer.stop();
            this.log.info("Embedded RedisServer {} stopped", this.redisServer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.log.error("Could not cleanly stop embedded RedisServer", e);
        }
    }
}
