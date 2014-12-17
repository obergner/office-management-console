package io.obergner.office.accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRef;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import static io.obergner.office.UuidValidation.checkAndConvert;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

public final class Account implements Serializable {

    private static final long serialVersionUID = 7954333235063995860L;

    private static final String MALFORMED_UUID_ERROR_CODE = "api.error.account.malformed-uuid";

    public static Account newAccount(final String name, final long mmaId, final String[] allowedOutChannels) {
        return new Account(UUID.randomUUID(), name, mmaId, System.currentTimeMillis(), allowedOutChannels, null);
    }

    @JsonProperty(value = "uuid", required = true)
    public final UUID uuid;

    @NotEmpty(message = "{account.validation.name.not-empty.message}")
    @JsonProperty(value = "name", required = true)
    public final String name;

    @Min(value = 1, message = "{account.validation.mmaId.positive.message}")
    @JsonProperty(value = "mmaId", required = true)
    public final long mmaId;

    @JsonProperty(value = "createdAt", required = true)
    public final long createdAt;

    @NotEmpty(message = "{account.validation.allowed_out_channels.not-empty.message}")
    @JsonProperty(value = "allowedOutChannels", required = true)
    public final String[] allowedOutChannels;

    @JsonProperty(value = "simsmeAccountRefCreation", required = false)
    public final SimsmeAccountRef simsmeAccountRef;

    public Account(final UUID uuid,
                   final String name,
                   final long mmaId,
                   final long createdAt,
                   final String[] allowedOutChannels) {
        this(uuid, name, mmaId, createdAt, allowedOutChannels, null);
    }

    public Account(final UUID uuid,
                   final String name,
                   final long mmaId,
                   final long createdAt,
                   final String[] allowedOutChannels,
                   final SimsmeAccountRef simsmeAccountRef) {
        notNull(uuid, "Argument 'uuid' must not be null");
        hasText(name, "Argument 'name' must neither be null nor empty");
        notEmpty(allowedOutChannels, "Argument 'allowedOutChannels' must neither be null nor empty");
        this.uuid = uuid;
        this.name = name;
        this.mmaId = mmaId;
        this.createdAt = createdAt;
        this.allowedOutChannels = allowedOutChannels;
        this.simsmeAccountRef = simsmeAccountRef != null ? simsmeAccountRef : SimsmeAccountRef.NULL;
    }

    public Account(final String uuid,
                   final String name,
                   final long mmaId,
                   final long createdAt,
                   final String[] allowedOutChannels) {
        this(uuid, name, mmaId, createdAt, allowedOutChannels, null);
    }

    @JsonCreator
    public Account(final @JsonProperty("uuid") String uuid,
                   final @JsonProperty("name") String name,
                   final @JsonProperty("mmaId") long mmaId,
                   final @JsonProperty("createdAt") long createdAt,
                   final @JsonProperty("allowedOutChannels") String[] allowedOutChannels,
                   final @JsonProperty("simsmeAccountRefCreation") SimsmeAccountRef simsmeAccountRef) {
        this.uuid = checkAndConvert(MALFORMED_UUID_ERROR_CODE, "account", "uuid", uuid);
        this.name = name;
        this.mmaId = mmaId;
        this.createdAt = createdAt;
        this.allowedOutChannels = allowedOutChannels;
        this.simsmeAccountRef = simsmeAccountRef;
    }

    public String allowedOutChannelsConcat() {
        final StringBuilder result = new StringBuilder();
        for (final String ch : this.allowedOutChannels) {
            result.append(ch).append(',');
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Account account = (Account) o;

        return createdAt == account.createdAt && mmaId == account.mmaId && Arrays.equals(allowedOutChannels, account.allowedOutChannels) && name.equals(account.name) && uuid.equals(account.uuid);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (int) (mmaId ^ (mmaId >>> 32));
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        result = 31 * result + Arrays.hashCode(allowedOutChannels);
        return result;
    }

    @Override
    public String toString() {
        return "Account[" +
                "uuid:" + uuid +
                "|name:'" + name + '\'' +
                "|mmaId:" + mmaId +
                "|createdAt:" + createdAt +
                "|allowedOutChannels:" + Arrays.toString(allowedOutChannels) +
                "|simsmeAccountRef:" + simsmeAccountRef +
                ']';
    }
}
