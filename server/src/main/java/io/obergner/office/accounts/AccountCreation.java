package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.ExistingSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.NoneSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class AccountCreation implements Serializable {

    private static final long serialVersionUID = -7668995662389526364L;

    public static class Builder {

        private String name;

        private long mmaId;

        private final List<String> allowedOutChannels = new ArrayList<>(5);

        private SimsmeAccountRefCreation simsmeAccountRefCreation;

        private Builder() {
        }

        public Builder withName(final String name) {
            hasText(name, "Argument 'name' must neither be null nor blank");
            this.name = name;
            return this;
        }

        public Builder withMmaId(final long mmaId) {
            this.mmaId = mmaId;
            return this;
        }

        public Builder withAllowedOutChannels(final String... allowedOutChannels) {
            Collections.addAll(this.allowedOutChannels, allowedOutChannels);
            return this;
        }

        public Builder withReferenceToExistingSimsmeAccount(final SimsmeGuid existingSimsmeAccountGuid) {
            notNull(existingSimsmeAccountGuid, "Argument 'existingSimsmeAccountGuid' must not be null");
            isTrue(this.simsmeAccountRefCreation == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefCreation = new ExistingSimsmeAccountRefCreation(existingSimsmeAccountGuid);
            return this;
        }

        public Builder withReferenceToNewSimsmeAccount(final String simsmeAccountName,
                                                       final String simsmeAccountImageBase64Jpeg) {
            isTrue(this.simsmeAccountRefCreation == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefCreation = new CreateNewSimsmeAccountRefCreation(simsmeAccountName, simsmeAccountImageBase64Jpeg);
            return this;
        }

        public AccountCreation build() {
            return new AccountCreation(this.name, this.mmaId, this.allowedOutChannels.toArray(new String[this.allowedOutChannels.size()]), this.simsmeAccountRefCreation);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

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

    @JsonCreator
    public AccountCreation(final @JsonProperty("name") String name,
                           final @JsonProperty("mmaId") long mmaId,
                           final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                           final @JsonProperty("simsmeAccountRefCreation") SimsmeAccountRefCreation simsmeAccountRefCreation) {
        this.name = name;
        this.mmaId = mmaId;
        this.allowedOutChannels = allowedOutChannels;
        this.simsmeAccountRefCreation = (simsmeAccountRefCreation != null) ? simsmeAccountRefCreation : NoneSimsmeAccountRefCreation.instance();
    }

    public boolean createsSimsmeAccountRef() {
        return this.simsmeAccountRefCreation.action != SimsmeAccountRefCreation.Action.none;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AccountCreation that = (AccountCreation) o;

        return this.mmaId == that.mmaId
                && Arrays.equals(this.allowedOutChannels, that.allowedOutChannels)
                && this.name.equals(that.name)
                && this.simsmeAccountRefCreation.equals(that.simsmeAccountRefCreation);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = (31 * result) + (int) (mmaId ^ (mmaId >>> 32));
        result = (31 * result) + Arrays.hashCode(allowedOutChannels);
        result = (31 * result) + simsmeAccountRefCreation.hashCode();
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
