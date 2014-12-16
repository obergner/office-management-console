package io.obergner.office.accounts.subaccounts.simsme;

import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SimsmeGuidTest {

    @Test
    public void should_correctly_parse_simsme_guid_string_representation() throws Exception {
        final Random randomPrefix = new Random(System.currentTimeMillis());
        final int prefix = randomPrefix.nextInt(1000);
        final UUID uuid = UUID.randomUUID();
        final String guidString = "" + prefix + ":{" + uuid.toString().toUpperCase() + "}";

        final SimsmeGuid parsedGuid = SimsmeGuid.parse(guidString);

        assertEquals(prefix, parsedGuid.prefix);
        assertEquals(uuid, parsedGuid.uuid);
    }
}
