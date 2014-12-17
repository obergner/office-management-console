package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SimsmeAccountRef implements Serializable {

    private static final long serialVersionUID = -6795998948530236852L;

    public static final SimsmeAccountRef NULL = new SimsmeAccountRef();

    @JsonCreator
    public static SimsmeAccountRef create(final @JsonProperty("simsmeGuid") SimsmeGuid simsmeGuid) {
        return simsmeGuid != null ? new SimsmeAccountRef(simsmeGuid) : NULL;
    }

    @JsonSerialize(using = SimsmeGuid.SimsmeGuidJsonSerializer.class)
    @JsonDeserialize(using = SimsmeGuid.SimsmeGuidJsonDeserializer.class)
    @JsonProperty("simsmeGuid")
    public final SimsmeGuid simsmeGuid;

    private SimsmeAccountRef(final SimsmeGuid simsmeGuid) {
        notNull(simsmeGuid, "Argument 'simsmeGuid' must not be null");
        this.simsmeGuid = simsmeGuid;
    }

    private SimsmeAccountRef() {
        this.simsmeGuid = null;
    }

    public Optional<SimsmeGuid> simsmeGuid() {
        return this.simsmeGuid != null ? Optional.of(this.simsmeGuid) : Optional.<SimsmeGuid>empty();
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
