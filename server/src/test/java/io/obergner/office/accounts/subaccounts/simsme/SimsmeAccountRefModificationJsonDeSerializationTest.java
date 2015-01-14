package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimsmeAccountRefModificationJsonDeSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void should_correctly_deserialize_ExistingSimsmeAccountRefCreation_from_json_representation() throws IOException {
        final SimsmeGuid existingSimsmeGuid = new SimsmeGuid(0, UUID.randomUUID());
        final String jsonRepresentation = "{\"action\":\"" + SimsmeAccountRefModification.Action.referenceExisting + "\",\"existingSimsmeGuid\":\"" + existingSimsmeGuid.toString() + "\"}";

        final SimsmeAccountRefModification deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, SimsmeAccountRefModification.class);

        assertTrue(deserialized instanceof ExistingSimsmeAccountRefCreation);
        assertEquals(existingSimsmeGuid, ExistingSimsmeAccountRefCreation.class.cast(deserialized).existingSimsmeGuid);
    }

    @Test
    public void should_correctly_serialize_ExistingSimsmeAccountRefCreation_to_json_representation() throws IOException {
        final SimsmeGuid existingSimsmeGuid = new SimsmeGuid(0, UUID.randomUUID());
        final ExistingSimsmeAccountRefCreation existingSimsmeAccountRefCreation = new ExistingSimsmeAccountRefCreation(existingSimsmeGuid);
        final String expectedJsonRepresentation = "{\"action\":\"" + existingSimsmeAccountRefCreation.action + "\",\"existingSimsmeGuid\":\"" + existingSimsmeAccountRefCreation.existingSimsmeGuid.toString() + "\"}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, existingSimsmeAccountRefCreation);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }

    @Test
    public void should_correctly_deserialize_CreateNewSimsmeAccountRefCreation_from_json_representation() throws IOException {
        final String jsonRepresentation = "{\"action\":\"" + SimsmeAccountRefModification.Action.createNew + "\"}";

        final SimsmeAccountRefModification deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, SimsmeAccountRefModification.class);

        assertTrue(deserialized instanceof CreateNewSimsmeAccountRefCreation);
    }

    @Test
    public void should_correctly_serialize_CreateNewSimsmeAccountRefCreation_without_optional_fields_to_json_representation() throws IOException {
        final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation(null, null);
        final String expectedJsonRepresentation = "{\"action\":\"" + createNewSimsmeAccountRefCreation.action + "\"}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, createNewSimsmeAccountRefCreation);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }

    @Test
    public void should_correctly_serialize_CreateNewSimsmeAccountRefCreation_with_all_optional_fields_to_json_representation() throws IOException {
        final String expectedName = "expectedName";
        final String expectedImageBase64Jpeg = "expectedImageBase64Jpeg";
        final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation(expectedName, expectedImageBase64Jpeg);
        final String expectedJsonRepresentation = "{\"action\":\"" + createNewSimsmeAccountRefCreation.action + "\",\"name\":\"" + expectedName + "\",\"imageBase64Jpeg\":\"" + expectedImageBase64Jpeg + "\"}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, createNewSimsmeAccountRefCreation);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }
}
