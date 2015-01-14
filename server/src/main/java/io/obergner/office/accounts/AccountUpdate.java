package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.ExistingSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefDeletion;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefModification;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class AccountUpdate extends AbstractAccountModification implements Serializable {

    private static final long serialVersionUID = 3994499985921459399L;

    public static class Builder {

        private UUID uuid;

        private String name;

        private long mmaId;

        private final List<String> allowedOutChannels = new ArrayList<>(5);

        private SimsmeAccountRefModification simsmeAccountRefModification;

        private Builder() {
        }

        public Builder withUuid(final UUID uuid) {
            notNull(uuid, "Argument 'uuid' must neither be null nor blank");
            this.uuid = uuid;
            return this;
        }

        public Builder withUuid(final String uuid) {
            hasText(uuid, "Argument 'uuid' must neither be null nor blank");
            this.uuid = UUID.fromString(uuid);
            return this;
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

        public Builder withDeletionOfExistingSimsmeAccountReference(final SimsmeGuid existingSimsmeAccountGuid) {
            notNull(existingSimsmeAccountGuid, "Argument 'existingSimsmeAccountGuid' must not be null");
            isTrue(this.simsmeAccountRefModification == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefModification = new SimsmeAccountRefDeletion(existingSimsmeAccountGuid);
            return this;
        }

        public Builder withReferenceToNewSimsmeAccount(final String simsmeAccountName,
                                                       final String simsmeAccountImageBase64Jpeg) {
            isTrue(this.simsmeAccountRefModification == null, "SimsmeAccountRefCreation has already been set");
            this.simsmeAccountRefModification = new CreateNewSimsmeAccountRefCreation(simsmeAccountName, simsmeAccountImageBase64Jpeg);
            return this;
        }

        public AccountUpdate build() {
            return new AccountUpdate(this.uuid, this.name, this.mmaId, this.allowedOutChannels.toArray(new String[this.allowedOutChannels.size()]), this.simsmeAccountRefModification);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonProperty(value = "uuid", required = true)
    public final UUID uuid;

    public AccountUpdate(final @JsonProperty("uuid") UUID uuid,
                         final @JsonProperty("name") String name,
                         final @JsonProperty("mmaId") long mmaId,
                         final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                         final @JsonProperty("simsmeAccountRefModification") SimsmeAccountRefModification simsmeAccountRefModification) {
        super(name, mmaId, allowedOutChannels, simsmeAccountRefModification);
        this.uuid = uuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final AccountUpdate that = (AccountUpdate) o;

        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AccountUpdate[" +
                "uuid:" + this.uuid +
                "|name:'" + name + '\'' +
                "|mmaId:" + mmaId +
                "|allowedOutChannels:" + Arrays.toString(allowedOutChannels) +
                "|simsmeAccountRefModification:" + simsmeAccountRefModification +
                ']';
    }
}
