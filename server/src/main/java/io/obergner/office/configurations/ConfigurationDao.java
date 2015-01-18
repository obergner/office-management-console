package io.obergner.office.configurations;

import java.util.List;

public interface ConfigurationDao {

    List<NamedConfiguration<?>> all();

    <T> NamedConfiguration<T> create(NamedConfiguration<T> configuration);
}
