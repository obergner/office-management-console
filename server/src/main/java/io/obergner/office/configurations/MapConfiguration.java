package io.obergner.office.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

public class MapConfiguration extends NamedConfiguration<Map<String, String>> {

    private static final long serialVersionUID = -8863447416479474858L;

    @JsonCreator
    public MapConfiguration(final @JsonProperty("name") String name,
                            final @JsonProperty("data") Map<String, String> data) {
        super(name, ConfigurationType.MAP, Collections.unmodifiableMap(data));
    }
}
