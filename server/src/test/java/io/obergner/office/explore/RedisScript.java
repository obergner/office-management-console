package io.obergner.office.explore;

import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Created by obergner on 01.11.14.
 */
public class RedisScript extends ExternalResource {

    public static final RedisScript named(final String scriptName, final Jedis redisClient) {
        return new RedisScript(scriptName, redisClient);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String scriptName;

    private final Jedis redisClient;

    private String scriptHash;

    private RedisScript(final String scriptName, final Jedis redisClient) {
        this.scriptName = scriptName;
        this.redisClient = redisClient;
    }

    public String scriptHash() {
        if (this.scriptHash == null)
            throw new IllegalStateException("Script '" + this.scriptName + "' has not been loaded yet");
        return this.scriptHash;
    }

    @Override
    protected void before() throws Throwable {
        this.log.info("Loading Redis Lua script '{}' ...", this.scriptName);
        final String script = IOUtils.toString(getClass().getClassLoader().getResource("scripts/" + this.scriptName));
        this.log.debug("Redis Lua script: {}", script);
        this.scriptHash = this.redisClient.scriptLoad(script);
        this.log.info("Redis Lua script '{}' successfully loaded - hash: {}", this.scriptName, this.scriptHash);
    }

    @Override
    protected void after() {
        super.after();
    }
}
