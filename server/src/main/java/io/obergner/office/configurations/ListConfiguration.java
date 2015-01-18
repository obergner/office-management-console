package io.obergner.office.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListConfiguration extends NamedConfiguration<List<String>> {

    private static final long serialVersionUID = -2548565112763345823L;

    @JsonCreator
    public ListConfiguration(final @JsonProperty("name") String name,
                             final @JsonProperty("data") List<String> data) {
        super(name, ConfigurationType.LIST, Collections.unmodifiableList(data));
    }

    public ListConfiguration(final String name, final String... data) {
        this(name, Arrays.asList(data));
    }
}
