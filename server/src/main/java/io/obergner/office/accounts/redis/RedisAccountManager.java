package io.obergner.office.accounts.redis;

import io.obergner.office.ApiErrorCode;
import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class RedisAccountManager implements AccountManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JedisPool redisClientPool;

    private LuaScriptRegistrar.ScriptHandles scriptHandles;

    public RedisAccountManager(final String redisHost, final int redisPort) {
        this(new JedisPool(new JedisPoolConfig(), redisHost, redisPort));
    }

    public RedisAccountManager(final JedisPool redisClientPool) {
        this.redisClientPool = redisClientPool;
    }

    @Override
    public Account accountByUuid(final UUID uuid) {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        Jedis redisClient = null;
        try {
            this.log.info("Looking up account by UUID [{}] ...", uuid);
            redisClient = this.redisClientPool.getResource();
            final Object result = redisClient.hmget(AccountSchema.Keys.accountUuid(uuid),
                    AccountSchema.Fields.UUID,
                    AccountSchema.Fields.NAME,
                    AccountSchema.Fields.MMA_ID,
                    AccountSchema.Fields.CREATED_AT,
                    AccountSchema.Fields.ALLOWED_OUT_CHANNELS);
            @SuppressWarnings("unchecked")
            final ArrayList<String> resultList = (ArrayList<String>) result;
            if (resultList.get(0) == null) {
                throw new JedisDataException(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND + ":No account with UUID [" + uuid.toString() + "] found");
            }
            final UUID accountUuid = UUID.fromString(resultList.get(0));
            final String name = resultList.get(1);
            final long mma = Long.parseLong(resultList.get(2));
            final long createdAt = Long.parseLong(resultList.get(3));
            final String[] allowedOutChannels = resultList.get(4).split(",");
            final Account account = new Account(accountUuid, name, mma, createdAt, allowedOutChannels);
            this.log.info("Successfully looked up account {}", account);

            return account;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    @Override
    public Account createAccount(final String name,
                                 final long mmaId,
                                 final String[] allowedOutChannels) {
        return createAccount(Account.newAccount(name, mmaId, allowedOutChannels));
    }

    @Override
    public Account createAccount(final Account newAccount) {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        checkNotNull(newAccount);
        Jedis redisClient = null;
        try {
            this.log.info("Creating [{}] ...", newAccount);
            redisClient = this.redisClientPool.getResource();
            final String optionalSimsmeAccountGuid = newAccount.simsmeAccountRef.simsmeGuid().map(simsmeGuid -> simsmeGuid.toString()).orElse(AccountSchema.NULL_VALUE);
            redisClient.evalsha(this.scriptHandles.createAccountScriptSha,
                    Collections.singletonList(AccountSchema.Keys.ACCOUNT_MMA_INDEX),
                    Arrays.asList(newAccount.uuid.toString(), newAccount.name, String.valueOf(newAccount.mmaId), String.valueOf(newAccount.createdAt), newAccount.allowedOutChannelsConcat(), optionalSimsmeAccountGuid));
            this.log.info("Successfully created new {}", newAccount);

            return newAccount;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    @Override
    public Account updateAccount(final Account account) {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        Jedis redisClient = null;
        try {
            this.log.info("Updating [{}] ...", account);
            redisClient = this.redisClientPool.getResource();
            redisClient.evalsha(this.scriptHandles.updateAccountScriptSha,
                    Collections.singletonList(AccountSchema.Keys.ACCOUNT_MMA_INDEX),
                    Arrays.asList(account.uuid.toString(), account.name, String.valueOf(account.mmaId), account.allowedOutChannelsConcat()));
            this.log.info("Successfully updated {}", account);

            return account;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    @Override
    public void deleteAccount(final UUID accountUuid) {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        Jedis redisClient = null;
        try {
            this.log.info("Deleting account [uuid:{}] ...", accountUuid);
            redisClient = this.redisClientPool.getResource();
            redisClient.evalsha(this.scriptHandles.deleteAccountScriptSha,
                    Collections.singletonList(AccountSchema.Keys.ACCOUNT_MMA_INDEX),
                    Arrays.asList(accountUuid.toString()));
            this.log.info("Successfully deleted account [uuid:{}]", accountUuid);
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    @Override
    public Account accountByMmaId(final long mmaId) {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        Jedis redisClient = null;
        try {
            this.log.info("Looking up account by MMA-ID [{}] ...", mmaId);
            redisClient = this.redisClientPool.getResource();
            final Object result = redisClient.evalsha(this.scriptHandles.getAccountByMmaIdScriptSha,
                    Collections.singletonList(AccountSchema.Keys.ACCOUNT_MMA_INDEX),
                    Collections.singletonList(String.valueOf(mmaId)));
            @SuppressWarnings("unchecked")
            final ArrayList<String> resultList = (ArrayList<String>) result;
            final UUID uuid = UUID.fromString(resultList.get(1));
            final String name = resultList.get(3);
            final long mma = Long.parseLong(resultList.get(5));
            final long createdAt = Long.parseLong(resultList.get(7));
            final String[] allowedOutChannels = resultList.get(9).split(",");
            final Account account = new Account(uuid, name, mma, createdAt, allowedOutChannels);
            this.log.info("Successfully looked up account {}", account);

            return account;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    @Override
    public List<Account> allAccounts() {
        checkState(this.scriptHandles != null, "RedisAccountManager has not yet been initialized - did you forget to call #initialize()?");
        Jedis redisClient = null;
        try {
            this.log.info("Looking up all accounts ...");
            redisClient = this.redisClientPool.getResource();
            final Object result = redisClient.evalsha(this.scriptHandles.getAllAccountsScriptSha);
            @SuppressWarnings("unchecked")
            final ArrayList<ArrayList<String>> resultList = (ArrayList<ArrayList<String>>) result;
            final List<Account> res = resultList
                    .stream()
                    .map((accountFields) -> new Account(UUID.fromString(accountFields.get(0)),
                            accountFields.get(1),
                            Long.parseLong(accountFields.get(2)),
                            Long.parseLong(accountFields.get(3)),
                            accountFields.get(4).split(",")))
                    .collect(Collectors.toList());
            this.log.info("Successfully looked up [{}] accounts", res.size());

            return res;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Lifecycle management
    // -----------------------------------------------------------------------------------------------------------------

    @PostConstruct
    public void initialize() throws IOException {
        this.log.info("Initializing {} ...", this);
        final LuaScriptRegistrar registrar = new LuaScriptRegistrar(this.redisClientPool);
        this.scriptHandles = registrar.register();
        this.log.info("Successfully initialized {}", this);
    }

    @PreDestroy
    public void destroy() {
        this.log.info("Destroying {} ...", this);
        this.redisClientPool.destroy();
        this.log.info("Successfully destroyed {}", this);
    }
}
