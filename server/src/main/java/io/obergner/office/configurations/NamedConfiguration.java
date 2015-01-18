package io.obergner.office.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ListConfiguration.class, name = "LIST"), @JsonSubTypes.Type(value = MapConfiguration.class, name = "MAP")})
@JsonPropertyOrder({"name", "type", "data"})
public class NamedConfiguration<T> implements Serializable {

    @JsonProperty(value = "name", required = true)
    public final String name;

    @JsonProperty(value = "type", required = true)
    public final ConfigurationType type;

    @JsonProperty(value = "data", required = true)
    public final T data;

    @JsonCreator
    public NamedConfiguration(final @JsonProperty("name") String name,
                              final @JsonProperty("type") ConfigurationType type,
                              final @JsonProperty("data") T data) {
        hasText(name, "Argument 'name' must neither be null nor empty");
        notNull(type, "Argument 'type' must not be null");
        notNull(data, "Argument 'data' must not be null");
        this.name = name;
        this.type = type;
        this.data = data;
    }

    @JsonIgnore
    public final ConfigurationId getId() {
        return new ConfigurationId(this.name, this.type);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NamedConfiguration that = (NamedConfiguration) o;

        return data.equals(that.data) && name.equals(that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NamedConfiguration[" +
                "name:'" + name + '\'' +
                "|type:" + type +
                "|data:" + data +
                ']';
    }
}
