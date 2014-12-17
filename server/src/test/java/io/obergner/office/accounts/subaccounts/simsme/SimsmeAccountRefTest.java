package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SimsmeAccountRefTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void should_correctly_deserialize_from_valid_json_representation() throws IOException {
        final SimsmeGuid expectedSimsmeGuid = new SimsmeGuid(99, UUID.randomUUID());
        final String jsonRepresentation = "{ \"simsmeGuid\" : \"" + expectedSimsmeGuid.toString() + "\" }";

        final SimsmeAccountRef deserializedAccountRef = OBJECT_MAPPER.readValue(jsonRepresentation, SimsmeAccountRef.class);

        assertEquals(expectedSimsmeGuid, deserializedAccountRef.simsmeGuid);
    }

    @Test
    public void should_correctly_serialize_SimsmeAccountRef() throws IOException {
        final SimsmeGuid expectedSimsmeGuid = new SimsmeGuid(99, UUID.randomUUID());
        final SimsmeAccountRef simsmeAccountRef = SimsmeAccountRef.create(expectedSimsmeGuid);
        final String jsonRepresentation = "{\"simsmeGuid\":\"" + expectedSimsmeGuid.toString() + "\"}";
        final StringWriter actualJsonRepresentation = new StringWriter();

        OBJECT_MAPPER.writeValue(actualJsonRepresentation, simsmeAccountRef);

        assertEquals(jsonRepresentation, actualJsonRepresentation.toString());
    }
}
