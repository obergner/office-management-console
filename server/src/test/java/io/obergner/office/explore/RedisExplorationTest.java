package io.obergner.office.explore;

import io.obergner.office.test.EmbeddedRedisServer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class RedisExplorationTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnDefaultPort();

    public static final RedisScript CREATE_ACCOUNT_SCRIPT = RedisScript.named("test-create-account.lua", EMBEDDED_REDIS_SERVER.client());

    public static final RedisScript GET_ACCOUNT_SCRIPT = RedisScript.named("test-get-account.lua", EMBEDDED_REDIS_SERVER.client());

    public static final RedisScript GET_ALL_ACCOUNTS_SCRIPT = RedisScript.named("test-get-all-accounts.lua", EMBEDDED_REDIS_SERVER.client());

    @ClassRule
    public static final RuleChain ORDERED_RULES = RuleChain.outerRule(EMBEDDED_REDIS_SERVER).around(CREATE_ACCOUNT_SCRIPT).around(GET_ACCOUNT_SCRIPT).around(GET_ALL_ACCOUNTS_SCRIPT);

    @Before
    public void cleanRedisDatabase() {
        EMBEDDED_REDIS_SERVER.client().flushDB();
    }

    @Test
    public void should_store_simple_key_value_pair_in_redis() {
        final String accountId = UUID.randomUUID().toString();

        final Jedis redisClient = EMBEDDED_REDIS_SERVER.client();

        final String mmaKey = "account:mma:123456789";
        redisClient.set(mmaKey, accountId);

        final String storedAccountId = redisClient.get(mmaKey);

        Assert.assertEquals(accountId, storedAccountId);
    }

    @Test
    public void should_successfully_load_create_account_lua_script() throws IOException {
        final String createAccountScript = IOUtils.toString(loadScriptFromClasspath("test-create-account.lua"));

        final String scriptSha = EMBEDDED_REDIS_SERVER.client().scriptLoad(createAccountScript);
        log.info("CREATE-SCRIPT-SHA: {}", scriptSha);

        Assert.assertNotNull(scriptSha);
    }

    private URL loadScriptFromClasspath(final String scriptName) {
        final URL resource = getClass().getClassLoader().getResource("scripts/" + scriptName);
        if (resource == null) {
            throw new IllegalArgumentException("Could not find script [" + scriptName + "] on classpath");
        }
        return resource;
    }

    @Test
    public void should_successfully_load_get_account_lua_script() throws IOException {
        final String createAccountScript = IOUtils.toString(loadScriptFromClasspath("test-get-account.lua"));

        final String scriptSha = EMBEDDED_REDIS_SERVER.client().scriptLoad(createAccountScript);
        log.info("GET-SCRIPT-SHA: {}", scriptSha);

        Assert.assertNotNull(scriptSha);
    }

    @Test(expected = JedisDataException.class)
    public void should_recognize_broken_lua_script() throws IOException {
        final String brokenScript = "return GARBAGE///";

        EMBEDDED_REDIS_SERVER.client().scriptLoad(brokenScript);
    }

    @Test
    public void should_successfully_create_new_account() throws IOException {
        final String accountUuid = UUID.randomUUID().toString();
        final String accountName = "ACCOUNT:" + accountUuid;
        final String mmaId = String.valueOf(123456789L);

        final Object result = EMBEDDED_REDIS_SERVER.client().evalsha(CREATE_ACCOUNT_SCRIPT.scriptHash(), Collections.singletonList("account:mma:index"), Arrays.asList(accountUuid, accountName, mmaId));
        this.log.info("RESULT: " + result);

        final boolean newAccountExists = EMBEDDED_REDIS_SERVER.client().hexists("account:uuid:" + accountUuid, "name");

        Assert.assertTrue(newAccountExists);
    }

    @Test
    public void should_update_secondary_m_m_a_index_when_creating_new_account() throws IOException {
        final String accountUuid = UUID.randomUUID().toString();
        final String accountName = "ACCOUNT:" + accountUuid;
        final String mmaId = String.valueOf(12345678912345L);

        final Object result = EMBEDDED_REDIS_SERVER.client().evalsha(CREATE_ACCOUNT_SCRIPT.scriptHash(), Collections.singletonList("account:mma:index"), Arrays.asList(accountUuid, accountName, mmaId));
        this.log.info("RESULT: " + result);

        final String indexedMMA = EMBEDDED_REDIS_SERVER.client().hget("account:mma:index", mmaId);

        Assert.assertEquals(accountUuid, indexedMMA);
    }

    @Test
    public void should_return_all_accounts() throws IOException {
        final String firstAccountUuid = UUID.randomUUID().toString();
        final String firstAccountName = "ACCOUNT:" + firstAccountUuid;
        final String firstMmaId = String.valueOf(123456789L);
        EMBEDDED_REDIS_SERVER.client().evalsha(CREATE_ACCOUNT_SCRIPT.scriptHash(), Collections.singletonList("account:mma:index"), Arrays.asList(firstAccountUuid, firstAccountName, firstMmaId));

        final String secondAccountUuid = UUID.randomUUID().toString();
        final String secondAccountName = "ACCOUNT:" + secondAccountUuid;
        final String secondMmaId = String.valueOf(12378124555L);
        EMBEDDED_REDIS_SERVER.client().evalsha(CREATE_ACCOUNT_SCRIPT.scriptHash(), Collections.singletonList("account:mma:index"), Arrays.asList(secondAccountUuid, secondAccountName, secondMmaId));

        final Object result = EMBEDDED_REDIS_SERVER.client().evalsha(GET_ALL_ACCOUNTS_SCRIPT.scriptHash());
        this.log.info("RESULT-ALL: " + result);
    }
}
