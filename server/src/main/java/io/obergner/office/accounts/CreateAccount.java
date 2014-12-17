package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.util.Arrays;

public class CreateAccount {

    @NotEmpty(message = "{account.validation.name.not-empty.message}")
    @JsonProperty(value = "name", required = true)
    public final String name;

    @Min(value = 1, message = "{account.validation.mmaId.positive.message}")
    @JsonProperty(value = "mmaId", required = true)
    public final long mmaId;

    @NotEmpty(message = "{account.validation.allowed_out_channels.not-empty.message}")
    @JsonProperty(value = "allowedOutChannels", required = true)
    public final String[] allowedOutChannels;

    @JsonCreator
    public CreateAccount(final @JsonProperty("name") String name,
                         final @JsonProperty("mmaId") long mmaId,
                         final @JsonProperty("allowedOutChannels") String[] allowedOutChannels) {
        this.name = name;
        this.mmaId = mmaId;
        this.allowedOutChannels = allowedOutChannels;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CreateAccount that = (CreateAccount) o;

        return mmaId == that.mmaId && Arrays.equals(allowedOutChannels, that.allowedOutChannels) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (mmaId ^ (mmaId >>> 32));
        result = 31 * result + Arrays.hashCode(allowedOutChannels);
        return result;
    }

    @Override
    public String toString() {
        return "CreateAccount[" +
                "name:'" + name + '\'' +
                "|mmaId:" + mmaId +
                "|allowedOutChannels:" + Arrays.toString(allowedOutChannels) +
                ']';
    }
}
