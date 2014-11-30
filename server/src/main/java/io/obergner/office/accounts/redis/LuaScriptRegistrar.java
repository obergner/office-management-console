package io.obergner.office.accounts.redis;

import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

class LuaScriptRegistrar {

    final class ScriptHandles {

        public final String createAccountScriptSha;

        public final String updateAccountScriptSha;

        public final String deleteAccountScriptSha;

        public final String getAccountByMmaIdScriptSha;

        public final String getAllAccountsScriptSha;

        ScriptHandles(final String createAccountScriptSha,
                      final String updateAccountScriptSha,
                      final String deleteAccountScriptSha,
                      final String getAccountByMmaIdScriptSha,
                      final String getAllAccountsScriptSha) {
            this.createAccountScriptSha = Preconditions.checkNotNull(createAccountScriptSha);
            this.updateAccountScriptSha = Preconditions.checkNotNull(updateAccountScriptSha);
            this.deleteAccountScriptSha = Preconditions.checkNotNull(deleteAccountScriptSha);
            this.getAccountByMmaIdScriptSha = Preconditions.checkNotNull(getAccountByMmaIdScriptSha);
            this.getAllAccountsScriptSha = Preconditions.checkNotNull(getAllAccountsScriptSha);
        }
    }

    private static final String CREATE_ACCOUNT_SCRIPT_NAME = "create-account.lua";

    private static final String UPDATE_ACCOUNT_SCRIPT_NAME = "update-account.lua";

    private static final String DELETE_ACCOUNT_SCRIPT_NAME = "delete-account.lua";

    private static final String GET_ACCOUNT_SCRIPT_NAME = "get-account-by-mmaid.lua";

    private static final String GET_ALL_ACCOUNTS_SCRIPT_NAME = "get-all-accounts.lua";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JedisPool redisClientPool;

    LuaScriptRegistrar(final JedisPool redisClientPool) {
        this.redisClientPool = Preconditions.checkNotNull(redisClientPool);
    }

    ScriptHandles register() throws IOException {
        this.log.info("Registering Lua scripts in Redis ...");
        final String createAccountHdl = registerScriptByName(CREATE_ACCOUNT_SCRIPT_NAME);
        final String updateAccountHdl = registerScriptByName(UPDATE_ACCOUNT_SCRIPT_NAME);
        final String deleteAccountHdl = registerScriptByName(DELETE_ACCOUNT_SCRIPT_NAME);
        final String getAccountHdl = registerScriptByName(GET_ACCOUNT_SCRIPT_NAME);
        final String getAllAccountsHdl = registerScriptByName(GET_ALL_ACCOUNTS_SCRIPT_NAME);
        this.log.info("Successfully registered all Lua scripts in Redis");
        return new ScriptHandles(createAccountHdl, updateAccountHdl, deleteAccountHdl, getAccountHdl, getAllAccountsHdl);
    }

    private String registerScriptByName(final String scriptName) throws IOException {
        this.log.info("Registering Lua script '{}' in Redis ...", scriptName);
        final String handle = registerScript(loadScript(scriptName));
        this.log.info("Successfully registered Lua script '{}' in Redis [sha:{}]", scriptName, handle);
        return handle;
    }

    private String registerScript(final String script) {
        try (final Jedis redisClient = this.redisClientPool.getResource()) {
            return redisClient.scriptLoad(script);
        }
    }

    private String loadScript(final String scriptName) throws IOException {
        final String scriptPath = "META-INF/scripts/" + scriptName;
        final URL scriptUrl = getClass().getClassLoader().getResource(scriptPath);
        if (scriptUrl == null) throw new FileNotFoundException(scriptPath);

        return IOUtils.toString(scriptUrl);
    }
}
