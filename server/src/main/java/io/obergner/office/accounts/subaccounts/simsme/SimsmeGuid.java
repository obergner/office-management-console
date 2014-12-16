package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.notNull;

public final class SimsmeGuid implements Serializable {

    private static final long serialVersionUID = -5366701909971836114L;

    private static final Pattern SIMSME_GUID_PATTERN = Pattern.compile("^(\\d{1,3}):\\{([0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12})\\}$");

    public static SimsmeGuid parse(final String simsmeGuidStr) {
        notNull(simsmeGuidStr, "Argument 'simsmeGuidStr' must not be null");
        final Matcher simsmeGuidMatcher = SIMSME_GUID_PATTERN.matcher(simsmeGuidStr);
        if (!simsmeGuidMatcher.find()) {
            throw new IllegalArgumentException("Input string ['" + simsmeGuidStr + "'] is not a valid SIMSme GUID");
        }
        final int prefix = Integer.parseInt(simsmeGuidMatcher.group(1));
        final UUID uuid = UUID.fromString(simsmeGuidMatcher.group(2));
        return new SimsmeGuid(prefix, uuid);
    }

    public final int prefix;

    public final UUID uuid;

    public SimsmeGuid(final int prefix, final UUID uuid) {
        notNull(uuid, "Argument 'uuid' must not be null");
        this.prefix = prefix;
        this.uuid = uuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SimsmeGuid that = (SimsmeGuid) o;

        return prefix == that.prefix && uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        int result = prefix;
        result = 31 * result + uuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "" + this.prefix + ":{" + this.uuid.toString().toUpperCase() + '}';
    }

    static final class SimsmeGuidJsonSerializer extends JsonSerializer<SimsmeGuid> {

        @Override
        public void serialize(final SimsmeGuid value,
                              final JsonGenerator jgen,
                              final SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }

    static final class SimsmeGuidJsonDeserializer extends JsonDeserializer<SimsmeGuid> {

        @Override
        public SimsmeGuid deserialize(final JsonParser jp,
                                      final DeserializationContext ctxt) throws IOException {
            return parse(jp.getText());
        }
    }
}
