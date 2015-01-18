package io.obergner.office.configurations;

import io.obergner.office.configurations.redis.ConfigurationSchema;
import org.springframework.util.StringUtils;

import java.io.Serializable;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public final class ConfigurationId implements Serializable, Comparable<ConfigurationId> {

    public static ConfigurationId parse(final String idString) {
        hasText(idString, "Argument 'idString' must neither be null nor empty");
        final String[] components = idString.split(":", 3);
        if (components.length != 3) {
            throw new IllegalArgumentException(String.format("Not a valid ConfigurationId: '%s'", idString));
        }
        if (!components[0].equals(ConfigurationSchema.Keys.NS)) {
            throw new IllegalArgumentException(String.format("Not a valid ConfigurationId: '%s'", idString));
        }
        final String name = components[1];
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(String.format("Not a valid ConfigurationId: '%s'", idString));
        }
        final ConfigurationType type = ConfigurationType.valueOf(components[2].toUpperCase());

        return new ConfigurationId(name, type);
    }

    public final String name;

    public final ConfigurationType type;

    public ConfigurationId(final String name, final ConfigurationType type) {
        hasText(name, "Argument 'name' must neither be null nor empty");
        notNull(type, "Argument 'type' must not be null");
        this.name = name;
        this.type = type;
    }

    @Override
    public int compareTo(final ConfigurationId o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ConfigurationId that = (ConfigurationId) o;

        return name.equals(that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "cfg:" + name + ":" + type.toString();
    }
}
