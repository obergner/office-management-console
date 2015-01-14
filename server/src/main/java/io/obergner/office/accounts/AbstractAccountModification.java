package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.NoneSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefModification;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AbstractAccountModification implements Serializable {

    @NotEmpty(message = "{account.validation.name.not-empty.message}")
    @JsonProperty(value = "name", required = true)
    public final String name;

    @Min(value = 1, message = "{account.validation.mmaId.positive.message}")
    @JsonProperty(value = "mmaId", required = true)
    public final long mmaId;

    @NotEmpty(message = "{account.validation.allowed_out_channels.not-empty.message}")
    @JsonProperty(value = "allowedOutChannels", required = true)
    public final String[] allowedOutChannels;

    @JsonProperty(value = "simsmeAccountRefModification", required = false)
    public final SimsmeAccountRefModification simsmeAccountRefModification;

    protected AbstractAccountModification(final @JsonProperty("name") String name,
                                          final @JsonProperty("mmaId") long mmaId,
                                          final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                                          final @JsonProperty("simsmeAccountRefModification") SimsmeAccountRefModification simsmeAccountRefModification) {
        this.mmaId = mmaId;
        this.simsmeAccountRefModification = (simsmeAccountRefModification != null) ? simsmeAccountRefModification : NoneSimsmeAccountRefCreation.instance();
        this.name = name;
        this.allowedOutChannels = allowedOutChannels;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractAccountModification that = (AbstractAccountModification) o;

        return this.mmaId == that.mmaId
                && Arrays.equals(this.allowedOutChannels, that.allowedOutChannels)
                && this.name.equals(that.name)
                && this.simsmeAccountRefModification.equals(that.simsmeAccountRefModification);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = (31 * result) + (int) (mmaId ^ (mmaId >>> 32));
        result = (31 * result) + Arrays.hashCode(allowedOutChannels);
        result = (31 * result) + simsmeAccountRefModification.hashCode();
        return result;
    }
}
