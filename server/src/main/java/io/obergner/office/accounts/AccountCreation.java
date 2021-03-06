package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.ExistingSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefModification;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class AccountCreation extends AbstractAccountModification {

    private static final long serialVersionUID = 2920148697629467274L;

    public static class Builder {

        private String name;

        private long mmaId;

        private final List<String> allowedOutChannels = new ArrayList<>(5);

        private SimsmeAccountRefModification simsmeAccountRefModification;

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
            isTrue(this.simsmeAccountRefModification == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefModification = new ExistingSimsmeAccountRefCreation(existingSimsmeAccountGuid);
            return this;
        }

        public Builder withReferenceToNewSimsmeAccount(final String simsmeAccountName,
                                                       final String simsmeAccountImageBase64Jpeg) {
            isTrue(this.simsmeAccountRefModification == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefModification = new CreateNewSimsmeAccountRefCreation(simsmeAccountName, simsmeAccountImageBase64Jpeg);
            return this;
        }

        public AccountCreation build() {
            return new AccountCreation(this.name, this.mmaId, this.allowedOutChannels.toArray(new String[this.allowedOutChannels.size()]), this.simsmeAccountRefModification);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonCreator
    public AccountCreation(final @JsonProperty("name") String name,
                           final @JsonProperty("mmaId") long mmaId,
                           final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                           final @JsonProperty("simsmeAccountRefModification") SimsmeAccountRefModification simsmeAccountRefModification) {
        super(name, mmaId, allowedOutChannels, simsmeAccountRefModification);
    }

    @Override
    public String toString() {
        return "AccountCreation[" +
                "name:'" + name + '\'' +
                "|mmaId:" + mmaId +
                "|allowedOutChannels:" + Arrays.toString(allowedOutChannels) +
                "|simsmeAccountRefModification:" + simsmeAccountRefModification +
                ']';
    }
}
