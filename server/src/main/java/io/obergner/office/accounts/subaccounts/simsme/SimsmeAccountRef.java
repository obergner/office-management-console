package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.util.Assert;

import java.io.Serializable;

public final class SimsmeAccountRef implements Serializable {

    private static final long serialVersionUID = -6795998948530236852L;

    @JsonSerialize(using = SimsmeGuid.SimsmeGuidJsonSerializer.class)
    @JsonDeserialize(using = SimsmeGuid.SimsmeGuidJsonDeserializer.class)
    @JsonProperty("simsmeGuid")
    public final SimsmeGuid simsmeGuid;

    @JsonCreator
    public SimsmeAccountRef(final @JsonProperty("simsmeGuid") SimsmeGuid simsmeGuid) {
        Assert.notNull(simsmeGuid, "Argument 'simsmeGuid' must not be null");
        this.simsmeGuid = simsmeGuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SimsmeAccountRef that = (SimsmeAccountRef) o;

        return simsmeGuid.equals(that.simsmeGuid);

    }

    @Override
    public int hashCode() {
        return simsmeGuid.hashCode();
    }

    @Override
    public String toString() {
        return "SimsmeAccountRef[" +
                "simsmeGuid:" + simsmeGuid +
                ']';
    }
}
