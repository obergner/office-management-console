package io.obergner.office.configurations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;

public class ConfigurationIdTest {

    @Rule
    public final TestName testName = new TestName();

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_null_input() throws Exception {
        ConfigurationId.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_empty_input() throws Exception {
        ConfigurationId.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_incorrect_namespace() throws Exception {
        ConfigurationId.parse("cf:cfg-name:LIST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_too_few_components() throws Exception {
        ConfigurationId.parse("cfg:cfg-name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_empty_config_name() throws Exception {
        ConfigurationId.parse("cfg::LIST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_empty_config_type() throws Exception {
        ConfigurationId.parse("cfg:cfg-name:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_should_reject_empty_illegal_config_type() throws Exception {
        ConfigurationId.parse("cfg:cfg-name:ILLEGAL");
    }

    @Test
    public void parse_should_successfully_parse_correct_input_string() throws Exception {
        final String expectedConfigName = this.testName.getMethodName();

        final ConfigurationId parsed = ConfigurationId.parse("cfg:" + expectedConfigName + ":" + ConfigurationType.LIST.toString());

        assertEquals(new ConfigurationId(expectedConfigName, ConfigurationType.LIST), parsed);
    }

    @Test
    public void parse_should_successfully_parse_correct_input_string_with_lowercase_type() throws Exception {
        final String expectedConfigName = this.testName.getMethodName();

        final ConfigurationId parsed = ConfigurationId.parse("cfg:" + expectedConfigName + ":" + ConfigurationType.MAP.toString().toLowerCase());

        assertEquals(new ConfigurationId(expectedConfigName, ConfigurationType.MAP), parsed);
    }
}
