package io.obergner.office.configurations.redis;

import io.obergner.office.configurations.ConfigurationDao;
import io.obergner.office.configurations.ConfigurationId;
import io.obergner.office.configurations.ListConfiguration;
import io.obergner.office.configurations.MapConfiguration;
import io.obergner.office.configurations.NamedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

public class RedisConfigurationDao implements ConfigurationDao {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JedisPool redisClientPool;

    public RedisConfigurationDao(final String redisHost, final int redisPort) {
        this(new JedisPool(new JedisPoolConfig(), redisHost, redisPort));
    }

    public RedisConfigurationDao(final JedisPool redisClientPool) {
        this.redisClientPool = redisClientPool;
    }

    @Override
    public List<NamedConfiguration<?>> all() {
        Jedis redisClient = null;
        try {
            this.log.debug("Looking up all configurations ...");
            redisClient = this.redisClientPool.getResource();
            final Jedis redisClientFinal = redisClient;
            final Set<String> allConfigKeys = redisClient.keys(ConfigurationSchema.Keys.ALL_CFG_KEYS_PATTERN);
            final List<NamedConfiguration<?>> result = new ArrayList<>(allConfigKeys.size());
            allConfigKeys.stream().map(ConfigurationId::parse).forEach(configId -> {
                final NamedConfiguration<?> config = get(configId, redisClientFinal);
                result.add(config);
            });
            this.log.debug("Successfully looked up [{}] configurations", allConfigKeys.size());

            return result;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    private <T> NamedConfiguration<T> get(final ConfigurationId id, final Jedis redisClient) {
        final NamedConfiguration<T> result;
        switch (id.type) {
            case LIST:
                final List<String> stringList = redisClient.lrange(id.toString(), 0L, 1000L);
                result = (NamedConfiguration<T>) new ListConfiguration(id.name, stringList);
                break;
            case MAP:
                final Map<String, String> map = redisClient.hgetAll(id.toString());
                result = (NamedConfiguration<T>) new MapConfiguration(id.name, map);
                break;
            default:
                throw new IllegalArgumentException("Unsupported configuration type: " + id.type);
        }
        return result;
    }

    @Override
    public <T> NamedConfiguration<T> create(final NamedConfiguration<T> configuration) {
        notNull(configuration, "Argument 'configuration' must not be null");
        final NamedConfiguration<T> result;
        switch (configuration.type) {
            case LIST:
                result = (NamedConfiguration<T>) createListConfiguration((NamedConfiguration<List<String>>) configuration);
                break;
            case MAP:
                result = (NamedConfiguration<T>) createMapConfiguration((NamedConfiguration<Map<String, String>>) configuration);
                break;
            default:
                throw new IllegalArgumentException("Unsupported configuration type: " + configuration.type);
        }
        return result;
    }

    private NamedConfiguration<List<String>> createListConfiguration(final NamedConfiguration<List<String>> listConfiguration) {
        Jedis redisClient = null;
        try {
            this.log.debug("Creating [{}] ...", listConfiguration);
            redisClient = this.redisClientPool.getResource();
            redisClient.rpush(ConfigurationSchema.Keys.configurationKey(listConfiguration), listConfiguration.data.toArray(new String[listConfiguration.data.size()]));
            this.log.debug("Successfully created new [{}]", listConfiguration);

            return listConfiguration;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }

    private NamedConfiguration<Map<String, String>> createMapConfiguration(final NamedConfiguration<Map<String, String>> mapConfiguration) {
        Jedis redisClient = null;
        try {
            this.log.debug("Creating [{}] ...", mapConfiguration);
            redisClient = this.redisClientPool.getResource();
            redisClient.hmset(ConfigurationSchema.Keys.configurationKey(mapConfiguration), mapConfiguration.data);
            this.log.debug("Successfully created new [{}]", mapConfiguration);

            return mapConfiguration;
        } finally {
            if (redisClient != null) {
                this.redisClientPool.returnResource(redisClient);
            }
        }
    }
}
