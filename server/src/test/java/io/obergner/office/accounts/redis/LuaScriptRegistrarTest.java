package io.obergner.office.accounts.redis;

import io.obergner.office.test.EmbeddedRedisServer;
import org.junit.ClassRule;
import org.junit.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LuaScriptRegistrarTest {

    private static final int REDIS_PORT = 7777;

    @ClassRule
    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnPort(REDIS_PORT);

    @Test
    public void should_successfully_register_all_scripts() throws IOException {
        final JedisPool redisClientPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", REDIS_PORT);
        final LuaScriptRegistrar objectUnderTest = new LuaScriptRegistrar(redisClientPool);

        final LuaScriptRegistrar.ScriptHandles handles = objectUnderTest.register();

        assertNotNull(handles);
        assertTrue(EMBEDDED_REDIS_SERVER.client().scriptExists(handles.createAccountScriptSha));
        assertTrue(EMBEDDED_REDIS_SERVER.client().scriptExists(handles.updateAccountScriptSha));
        assertTrue(EMBEDDED_REDIS_SERVER.client().scriptExists(handles.deleteAccountScriptSha));
        assertTrue(EMBEDDED_REDIS_SERVER.client().scriptExists(handles.getAccountByMmaIdScriptSha));
        assertTrue(EMBEDDED_REDIS_SERVER.client().scriptExists(handles.getAllAccountsScriptSha));
    }
}
