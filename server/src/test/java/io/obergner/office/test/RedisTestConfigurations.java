package io.obergner.office.test;

import io.obergner.office.configurations.ConfigurationDao;
import io.obergner.office.configurations.ListConfiguration;
import io.obergner.office.configurations.MapConfiguration;
import io.obergner.office.configurations.NamedConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RedisTestConfigurations {

    public static final List<ListConfiguration> ALL_LIST_CONFIGURATIONS = IntStream.range(0, 10)
            .mapToObj(RedisTestConfigurations::listConfigurationForIdx)
            .collect(Collectors.toList());

    private static ListConfiguration listConfigurationForIdx(final int idx) {
        final String[] data = new String[]{"data 1 " + idx, "data 2 " + idx, "data 3 " + idx};
        return new ListConfiguration("list-configuration-" + idx, data);
    }

    public static final List<MapConfiguration> ALL_MAP_CONFIGURATIONS = IntStream.range(0, 10)
            .mapToObj(RedisTestConfigurations::mapConfigurationForIdx)
            .collect(Collectors.toList());

    private static MapConfiguration mapConfigurationForIdx(final int idx) {
        final Map<String, String> data = new HashMap<>(3);
        data.put("key 1 " + idx, "value 1 " + idx);
        data.put("key 2 " + idx, "value 2 " + idx);
        data.put("key 3 " + idx, "value 3 " + idx);
        return new MapConfiguration("map-configuration-" + idx, data);
    }

    public static final List<NamedConfiguration<?>> ALL_CONFIGURATIONS;

    static {
        final List<NamedConfiguration<?>> tmp = new ArrayList<>(ALL_LIST_CONFIGURATIONS.size() + ALL_MAP_CONFIGURATIONS.size());
        tmp.addAll(ALL_LIST_CONFIGURATIONS);
        tmp.addAll(ALL_MAP_CONFIGURATIONS);
        ALL_CONFIGURATIONS = Collections.unmodifiableList(tmp);
    }

    public static void storeUsing(final ConfigurationDao configurationDao) {
        ALL_CONFIGURATIONS.stream().forEach(configurationDao::create);
    }
}
