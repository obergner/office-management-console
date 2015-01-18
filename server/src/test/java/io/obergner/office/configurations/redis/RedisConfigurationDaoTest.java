package io.obergner.office.configurations.redis;

import io.obergner.office.configurations.ListConfiguration;
import io.obergner.office.configurations.MapConfiguration;
import io.obergner.office.configurations.NamedConfiguration;
import io.obergner.office.test.EmbeddedRedisServer;
import io.obergner.office.test.RedisTestConfigurations;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RedisConfigurationDaoTest {

    private static final int PORT = 6380;

    @ClassRule
    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnPort(PORT);

    private static final RedisConfigurationDao OBJECT_UNDER_TEST = new RedisConfigurationDao("127.0.0.1", PORT);

    @Before
    public void prepareRedis() {
        RedisTestConfigurations.storeUsing(OBJECT_UNDER_TEST);
    }

    @After
    public void clearRedis() {
        EMBEDDED_REDIS_SERVER.client().flushDB();
    }

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void all_should_return_all_configurations_stored_in_redis() {
        final List<NamedConfiguration<?>> allConfigs = OBJECT_UNDER_TEST.all();

        assertEquals(RedisTestConfigurations.ALL_CONFIGURATIONS.size(), allConfigs.size());
    }

    @Test
    public void create_should_store_list_configuration_in_redis() throws Exception {
        final String[] data = new String[]{"data 1", "data 2"};
        final ListConfiguration listConfig = new ListConfiguration(this.testName.getMethodName(), data);

        final NamedConfiguration<List<String>> storedConfig = OBJECT_UNDER_TEST.create(listConfig);

        assertNotNull(storedConfig);

        final boolean configExists = EMBEDDED_REDIS_SERVER.client().exists(ConfigurationSchema.Keys.configurationKey(listConfig));
        assertTrue(configExists);

        final List<String> storedData = EMBEDDED_REDIS_SERVER.client().lrange(ConfigurationSchema.Keys.configurationKey(listConfig), 0, 1000);
        assertArrayEquals(data, storedData.toArray(new String[storedData.size()]));
    }

    @Test
    public void create_should_store_map_configuration_in_redis() throws Exception {
        final Map<String, String> data = new HashMap<>(2);
        data.put("key 1", "value 1");
        data.put("key 2", "value 2");
        final MapConfiguration mapConfig = new MapConfiguration(this.testName.getMethodName(), data);

        final NamedConfiguration<Map<String, String>> storedConfig = OBJECT_UNDER_TEST.create(mapConfig);

        assertNotNull(storedConfig);

        final boolean configExists = EMBEDDED_REDIS_SERVER.client().exists(ConfigurationSchema.Keys.configurationKey(mapConfig));
        assertTrue(configExists);

        final Map<String, String> storedData = EMBEDDED_REDIS_SERVER.client().hgetAll(ConfigurationSchema.Keys.configurationKey(mapConfig));
        assertEquals(data, storedData);
    }
}
