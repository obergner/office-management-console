package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefCreation;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class AccountCreation implements Serializable {

    private static final long serialVersionUID = -7668995662389526364L;

    @NotEmpty(message = "{account.validation.name.not-empty.message}")
    @JsonProperty(value = "name", required = true)
    public final String name;

    @Min(value = 1, message = "{account.validation.mmaId.positive.message}")
    @JsonProperty(value = "mmaId", required = true)
    public final long mmaId;

    @NotEmpty(message = "{account.validation.allowed_out_channels.not-empty.message}")
    @JsonProperty(value = "allowedOutChannels", required = true)
    public final String[] allowedOutChannels;

    @JsonProperty(value = "simsmeAccountRefCreation", required = false)
    public final SimsmeAccountRefCreation simsmeAccountRefCreation;

    public AccountCreation(final String name,
                           final long mmaId,
                           final String[] allowedOutChannels) {
        this(name, mmaId, allowedOutChannels, null);
    }

    @JsonCreator
    public AccountCreation(final @JsonProperty("name") String name,
                           final @JsonProperty("mmaId") long mmaId,
                           final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                           final @JsonProperty("simsmeAccountRefCreation") SimsmeAccountRefCreation simsmeAccountRefCreation) {
        this.name = name;
        this.mmaId = mmaId;
        this.allowedOutChannels = allowedOutChannels;
        this.simsmeAccountRefCreation = simsmeAccountRefCreation;
    }

    public boolean createsSimsmeAccountRef() {
        return this.simsmeAccountRefCreation != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AccountCreation that = (AccountCreation) o;

        return mmaId == that.mmaId
                && Arrays.equals(allowedOutChannels, that.allowedOutChannels)
                && name.equals(that.name)
                && !(simsmeAccountRefCreation != null ? !simsmeAccountRefCreation.equals(that.simsmeAccountRefCreation) : that.simsmeAccountRefCreation != null);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (mmaId ^ (mmaId >>> 32));
        result = 31 * result + Arrays.hashCode(allowedOutChannels);
        result = 31 * result + (simsmeAccountRefCreation != null ? simsmeAccountRefCreation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccountCreation[" +
                "name:'" + name + '\'' +
                "|mmaId:" + mmaId +
                "|allowedOutChannels:" + Arrays.toString(allowedOutChannels) +
                "|simsmeAccountRefCreation:" + simsmeAccountRefCreation +
                ']';
    }
}
