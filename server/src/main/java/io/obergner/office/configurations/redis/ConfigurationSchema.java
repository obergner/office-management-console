package io.obergner.office.configurations.redis;

import io.obergner.office.configurations.ConfigurationType;
import io.obergner.office.configurations.NamedConfiguration;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public final class ConfigurationSchema {

    public static class Keys {

        public static final String NS = "cfg";

        public static final String ALL_CFG_KEYS_PATTERN = NS + ":*";

        public static String configurationKey(final NamedConfiguration<?> namedConfiguration) {
            notNull(namedConfiguration, "Argument 'namedConfiguration' must not be null");
            return configurationKey(namedConfiguration.name, namedConfiguration.type);
        }

        public static String configurationKey(final String name, final ConfigurationType type) {
            hasText(name, "Argument 'name' must neither be null nor empty");
            notNull(type, "Argument 'type' must not be null");
            return String.format(NS + ":%s:%s", name, type.toString().toLowerCase());
        }
    }

    private ConfigurationSchema() {
        // Do not instantiate
    }
}
