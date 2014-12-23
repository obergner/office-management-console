package io.obergner.office.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class AccountCreationJsonDeSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void should_correctly_deserialze_AccountCreation_without_optional_fields() throws IOException {
        final String expectedName = "expectedName";
        final int expectedMmaId = 1232234;
        final String[] expectedAllowedOutChannel = new String[]{"channel1", "channel2"};
        final AccountCreation expectedAccountCreation = AccountCreation.newBuilder()
                .withName(expectedName)
                .withMmaId(expectedMmaId)
                .withAllowedOutChannels(expectedAllowedOutChannel)
                .build();

        final String jsonRepresentation = "{\"name\":\"" + expectedName +
                "\",\"mmaId\":" + expectedMmaId +
                ",\"allowedOutChannels\":[\"" + expectedAllowedOutChannel[0] + "\",\"" + expectedAllowedOutChannel[1] + "\"]}";

        final AccountCreation deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, AccountCreation.class);

        assertEquals(expectedAccountCreation, deserialized);
    }

    @Test
    public void should_correctly_serialze_AccountCreation_without_optional_fields() throws IOException {
        final String expectedName = "expectedName";
        final int expectedMmaId = 1232234;
        final String[] expectedAllowedOutChannel = new String[]{"channel1", "channel2"};
        final AccountCreation expectedAccountCreation = AccountCreation.newBuilder()
                .withName(expectedName)
                .withMmaId(expectedMmaId)
                .withAllowedOutChannels(expectedAllowedOutChannel)
                .build();
        final String expectedJsonRepresentation = "{\"name\":\"" + expectedName +
                "\",\"mmaId\":" + expectedMmaId +
                ",\"allowedOutChannels\":[\"" + expectedAllowedOutChannel[0] + "\",\"" + expectedAllowedOutChannel[1] + "\"]"
                + ",\"simsmeAccountRefCreation\":{\"action\":\"none\"}}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, expectedAccountCreation);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }

    @Test
    public void should_correctly_deserialze_AccountCreation_with_CreateNewSimmseAccountRefCreation() throws IOException {
        final CreateNewSimsmeAccountRefCreation expectedCreateNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation("simsmeAccountName", "simsmeAccountImage");
        final AccountCreation expectedAccountCreation = AccountCreation.newBuilder()
                .withName("expectedName")
                .withMmaId(7823417654L)
                .withAllowedOutChannels("channel1", "channel2")
                .withReferenceToNewSimsmeAccount(expectedCreateNewSimsmeAccountRefCreation.name, expectedCreateNewSimsmeAccountRefCreation.imageBase64Jpeg)
                .build();

        final String jsonRepresentation = "{\"name\":\"" + expectedAccountCreation.name +
                "\",\"mmaId\":" + expectedAccountCreation.mmaId +
                ",\"allowedOutChannels\":[\"" + expectedAccountCreation.allowedOutChannels[0] + "\",\"" + expectedAccountCreation.allowedOutChannels[1] + "\"]" +
                ",\"simsmeAccountRefCreation\":{\"action\":\"" + expectedCreateNewSimsmeAccountRefCreation.action.toString() +
                "\",\"name\":\"" + expectedCreateNewSimsmeAccountRefCreation.name +
                "\",\"imageBase64Jpeg\":\"" + expectedCreateNewSimsmeAccountRefCreation.imageBase64Jpeg + "\"}}";

        final AccountCreation deserialized = OBJECT_MAPPER.readValue(jsonRepresentation, AccountCreation.class);

        assertEquals(expectedAccountCreation, deserialized);
    }

    @Test
    public void should_correctly_serialze_AccountCreation_with_CreateNewSimmseAccountRefCreation() throws IOException {
        final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation("simsmeAccountName", "simsmeAccountImage");
        final AccountCreation accountCreation = AccountCreation.newBuilder()
                .withName("expectedName")
                .withMmaId(7823417654L)
                .withAllowedOutChannels("channel1", "channel2")
                .withReferenceToNewSimsmeAccount(createNewSimsmeAccountRefCreation.name, createNewSimsmeAccountRefCreation.imageBase64Jpeg)
                .build();

        final String expectedJsonRepresentation = "{\"name\":\"" + accountCreation.name +
                "\",\"mmaId\":" + accountCreation.mmaId +
                ",\"allowedOutChannels\":[\"" + accountCreation.allowedOutChannels[0] + "\",\"" + accountCreation.allowedOutChannels[1] + "\"]" +
                ",\"simsmeAccountRefCreation\":{\"action\":\"" + createNewSimsmeAccountRefCreation.action.toString() +
                "\",\"name\":\"" + createNewSimsmeAccountRefCreation.name +
                "\",\"imageBase64Jpeg\":\"" + createNewSimsmeAccountRefCreation.imageBase64Jpeg + "\"}}";

        final StringWriter actualJsonRepresentation = new StringWriter();
        OBJECT_MAPPER.writeValue(actualJsonRepresentation, accountCreation);

        assertEquals(expectedJsonRepresentation, actualJsonRepresentation.toString());
    }
}
