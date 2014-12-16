package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static org.springframework.util.Assert.notNull;

public final class ExistingSimsmeAccountRefCreation extends SimsmeAccountRefCreation {

    private static final long serialVersionUID = 3251421842175077683L;

    @JsonSerialize(using = SimsmeGuid.SimsmeGuidJsonSerializer.class)
    @JsonDeserialize(using = SimsmeGuid.SimsmeGuidJsonDeserializer.class)
    @JsonProperty("existingSimsmeGuid")
    public final SimsmeGuid existingSimsmeGuid;

    @JsonCreator
    public ExistingSimsmeAccountRefCreation(final @JsonProperty("existingSimsmeGuid") SimsmeGuid existingSimmsmeGuid) {
        super(Action.referenceExisting);
        notNull(existingSimmsmeGuid, "Argument 'existingSimsmeGuid' must not be null");
        this.existingSimsmeGuid = existingSimmsmeGuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExistingSimsmeAccountRefCreation that = (ExistingSimsmeAccountRefCreation) o;

        return existingSimsmeGuid.equals(that.existingSimsmeGuid);

    }

    @Override
    public int hashCode() {
        return existingSimsmeGuid.hashCode();
    }

    @Override
    public String toString() {
        return "ExistingSimsmeAccountRefCreation[" +
                "existingSimsmeGuid:" + existingSimsmeGuid +
                ']';
    }
}
