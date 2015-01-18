package io.obergner.office.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NamedConfigurationJsonDeSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void should_correctly_serialize_list_configuration() throws IOException {
        final String[] data = new String[]{"data one", "data two"};
        final NamedConfiguration<List<String>> objectUnderTest = new ListConfiguration(this.testName.getMethodName(), data);

        final ObjectNode expectedSerialization = OBJECT_MAPPER.createObjectNode();
        expectedSerialization.put("name", objectUnderTest.name);
        expectedSerialization.put("type", objectUnderTest.type.toString());
        final ArrayNode arrayNode = expectedSerialization.putArray("data");
        objectUnderTest.data.forEach(arrayNode::add);

        final StringWriter actualSerialization = new StringWriter();
        OBJECT_MAPPER.writeValue(actualSerialization, objectUnderTest);

        assertEquals(OBJECT_MAPPER.writeValueAsString(expectedSerialization), actualSerialization.toString());
    }

    @Test
    public void should_correctly_deserialize_list_configuration() throws IOException {
        final String expectedName = this.testName.getMethodName();
        final ConfigurationType expectedType = ConfigurationType.LIST;
        final String[] expectedData = new String[]{"data one", "data two"};

        final ObjectNode expectedSerialization = OBJECT_MAPPER.createObjectNode();
        expectedSerialization.put("name", expectedName);
        expectedSerialization.put("type", expectedType.toString());
        final ArrayNode arrayNode = expectedSerialization.putArray("data");
        Arrays.asList(expectedData).forEach(arrayNode::add);
        final String serialized = OBJECT_MAPPER.writeValueAsString(expectedSerialization);

        final NamedConfiguration<List<String>> deserialized = OBJECT_MAPPER.readValue(serialized, new TypeReference<NamedConfiguration<List<String>>>() {
        });

        assertEquals(expectedName, deserialized.name);
        assertEquals(expectedType, deserialized.type);
        assertArrayEquals(expectedData, deserialized.data.toArray(new String[deserialized.data.size()]));
    }

    @Test
    public void should_correctly_serialize_map_configuration() throws IOException {
        final Map<String, String> data = new HashMap<>();
        data.put("key 1", "value 1");
        data.put("key 2", "value 2");
        final NamedConfiguration<Map<String, String>> objectUnderTest = new MapConfiguration(this.testName.getMethodName(), data);

        final ObjectNode expectedSerialization = OBJECT_MAPPER.createObjectNode();
        expectedSerialization.put("name", objectUnderTest.name);
        expectedSerialization.put("type", objectUnderTest.type.toString());
        final ObjectNode mapNode = expectedSerialization.putObject("data");
        objectUnderTest.data.forEach(mapNode::put);

        final StringWriter actualSerialization = new StringWriter();
        OBJECT_MAPPER.writeValue(actualSerialization, objectUnderTest);

        assertEquals(OBJECT_MAPPER.writeValueAsString(expectedSerialization), actualSerialization.toString());
    }

    @Test
    public void should_correctly_deserialize_map_configuration() throws IOException {
        final String expectedName = this.testName.getMethodName();
        final ConfigurationType expectedType = ConfigurationType.MAP;
        final Map<String, String> expectedData = new HashMap<>();
        expectedData.put("key 1", "value 1");
        expectedData.put("key 2", "value 2");

        final ObjectNode expectedSerialization = OBJECT_MAPPER.createObjectNode();
        expectedSerialization.put("name", expectedName);
        expectedSerialization.put("type", expectedType.toString());
        final ObjectNode mapNode = expectedSerialization.putObject("data");
        expectedData.forEach(mapNode::put);
        final String serialized = OBJECT_MAPPER.writeValueAsString(expectedSerialization);

        final NamedConfiguration<List<String>> deserialized = OBJECT_MAPPER.readValue(serialized, new TypeReference<NamedConfiguration<Map<String, String>>>() {
        });

        assertEquals(expectedName, deserialized.name);
        assertEquals(expectedType, deserialized.type);
        assertEquals(expectedData, deserialized.data);
    }
}
