package io.obergner.office.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AccountUpdateJsonDeSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void should_correctly_deserialize_AccountUpdate_without_optional_fields() throws IOException {
        final UUID expectedUuid = UUID.randomUUID();
        final String expectedName = "expectedName";
        final int expectedMmaId = 1232234;
        final String[] expectedAllowedOutChannel = new String[]{"channel1", "channel2"};
        final AccountUpdate expectedAccountUpdate = AccountUpdate.newBuilder()
                .withUuid(expectedUuid)
                .withName(expectedName)
                .withMmaId(expectedMmaId)
                .withAllowedOutChannels(expectedAllowedOutChannel)
                .build();

        final String jsonRepresentation = "{\"uuid\":\"" + expectedUuid +
                "\",\"name\":\"" + expectedName +
                "\",\"mmaId\":" + expectedMmaId +
                ",\"allowedOutChannels\":[\"" + expectedAllowedOutChannel[0] + "\",\"" + expectedAllowedOutChannel[1] + "\"]}";

        final AccountUpdate deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, AccountUpdate.class);

        assertEquals(expectedAccountUpdate, deserialized);
    }

    @Test
    public void should_correctly_serialize_AccountUpdate_without_optional_fields() throws IOException {
        final UUID expectedUuid = UUID.randomUUID();
        final String expectedName = "expectedName";
        final int expectedMmaId = 1232234;
        final String[] expectedAllowedOutChannel = new String[]{"channel1", "channel2"};
        final AccountUpdate expectedAccountUpdate = AccountUpdate.newBuilder()
                .withUuid(expectedUuid)
                .withName(expectedName)
                .withMmaId(expectedMmaId)
                .withAllowedOutChannels(expectedAllowedOutChannel)
                .build();
        final String expectedJsonRepresentation = "{\"uuid\":\"" + expectedUuid +
                "\",\"name\":\"" + expectedName +
                "\",\"mmaId\":" + expectedMmaId +
                ",\"allowedOutChannels\":[\"" + expectedAllowedOutChannel[0] + "\",\"" + expectedAllowedOutChannel[1] + "\"]"
                + ",\"simsmeAccountRefModification\":{\"action\":\"none\"}}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, expectedAccountUpdate);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }

    @Test
    public void should_correctly_deserialize_AccountUpdate_with_CreateNewSimmseAccountRefCreation() throws IOException {
        final CreateNewSimsmeAccountRefCreation expectedCreateNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation("simsmeAccountName", "simsmeAccountImage");
        final AccountUpdate expectedAccountUpdate = AccountUpdate.newBuilder()
                .withUuid(UUID.randomUUID())
                .withName("expectedName")
                .withMmaId(7823417654L)
                .withAllowedOutChannels("channel1", "channel2")
                .withReferenceToNewSimsmeAccount(expectedCreateNewSimsmeAccountRefCreation.name, expectedCreateNewSimsmeAccountRefCreation.imageBase64Jpeg)
                .build();

        final String jsonRepresentation = "{\"uuid\":\"" + expectedAccountUpdate.uuid +
                "\",\"name\":\"" + expectedAccountUpdate.name +
                "\",\"mmaId\":" + expectedAccountUpdate.mmaId +
                ",\"allowedOutChannels\":[\"" + expectedAccountUpdate.allowedOutChannels[0] + "\",\"" + expectedAccountUpdate.allowedOutChannels[1] + "\"]" +
                ",\"simsmeAccountRefModification\":{\"action\":\"" + expectedCreateNewSimsmeAccountRefCreation.action.toString() +
                "\",\"name\":\"" + expectedCreateNewSimsmeAccountRefCreation.name +
                "\",\"imageBase64Jpeg\":\"" + expectedCreateNewSimsmeAccountRefCreation.imageBase64Jpeg + "\"}}";

        final AccountUpdate deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, AccountUpdate.class);

        assertEquals(expectedAccountUpdate, deserialized);
    }

    @Test
    public void should_correctly_serialize_AccountUpdate_with_CreateNewSimmseAccountRefCreation() throws IOException {
        final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation("simsmeAccountName", "simsmeAccountImage");
        final AccountUpdate accountUpdate = AccountUpdate.newBuilder()
                .withUuid(UUID.randomUUID())
                .withName("expectedName")
                .withMmaId(7823417654L)
                .withAllowedOutChannels("channel1", "channel2")
                .withReferenceToNewSimsmeAccount(createNewSimsmeAccountRefCreation.name, createNewSimsmeAccountRefCreation.imageBase64Jpeg)
                .build();

        final String expectedJsonRepresentation = "{\"uuid\":\"" + accountUpdate.uuid +
                "\",\"name\":\"" + accountUpdate.name +
                "\",\"mmaId\":" + accountUpdate.mmaId +
                ",\"allowedOutChannels\":[\"" + accountUpdate.allowedOutChannels[0] + "\",\"" + accountUpdate.allowedOutChannels[1] + "\"]" +
                ",\"simsmeAccountRefModification\":{\"action\":\"" + createNewSimsmeAccountRefCreation.action.toString() +
                "\",\"name\":\"" + createNewSimsmeAccountRefCreation.name +
                "\",\"imageBase64Jpeg\":\"" + createNewSimsmeAccountRefCreation.imageBase64Jpeg + "\"}}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, accountUpdate);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }
}
