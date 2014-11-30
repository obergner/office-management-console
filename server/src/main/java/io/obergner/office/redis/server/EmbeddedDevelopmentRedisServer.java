package io.obergner.office.redis.server;

import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.redis.AccountSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class EmbeddedDevelopmentRedisServer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final int port;

    private final RedisServer redisServer;

    public EmbeddedDevelopmentRedisServer(final int port) throws IOException {
        this.port = port;
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void start() throws IOException {
        this.log.info("Starting embedded DEVELOPMENT RedisServer {} on port [{}] ...", this.redisServer, this.port);
        this.redisServer.start();
        populateWithTestData();
        this.log.info("Started embedded DEVELOPMENT RedisServer {} on port [{}]", this.redisServer, this.port);
    }

    private void populateWithTestData() {
        this.log.info("Populating embedded DEVELOPMENT RedisServer with test data ...");
        final Jedis redisClient = new Jedis("127.0.0.1", this.port);
        IntStream.range(0, 100)
                .mapToObj((idx) -> testAccountForIdx(idx))
                .forEach(
                        account -> {
                            final Map<String, String> accMap = new HashMap<>();
                            accMap.put(AccountSchema.Fields.UUID, account.uuid.toString());
                            accMap.put(AccountSchema.Fields.NAME, account.name);
                            accMap.put(AccountSchema.Fields.MMA_ID, String.valueOf(account.mmaId));
                            accMap.put(AccountSchema.Fields.CREATED_AT, String.valueOf(account.createdAt));
                            accMap.put(AccountSchema.Fields.ALLOWED_OUT_CHANNELS, account.allowedOutChannelsConcat());
                            redisClient.hmset(AccountSchema.Keys.accountUuid(account.uuid), accMap);
                        });
        this.log.info("Populated embedded DEVELOPMENT RedisServer with test data");
    }

    private Account testAccountForIdx(final int idx) {
        return Account.newAccount("Test Account " + idx, 12000000 + idx, new String[]{"USSD", "FlashSMS", "SIMSme"});
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        this.log.info("Stopping embedded DEVELOPMENT RedisServer {} on port [{}] ...", this.redisServer, this.port);
        this.redisServer.stop();
        this.log.info("Stopped embedded DEVELOPMENT RedisServer {} on port [{}]", this.redisServer, this.port);
    }
}
