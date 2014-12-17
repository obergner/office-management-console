package io.obergner.office.accounts.subaccounts.simsme;

import java.io.Serializable;
import java.util.UUID;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public final class SimsmeAccount implements Serializable {

    private static final long serialVersionUID = 3284617208504475201L;

    private static final int GUID_PREFIX = 0;

    public final SimsmeGuid guid;

    public final String name;

    public final String imageBase64Jpeg;

    public SimsmeAccount(final String name, final String imageBase64Jpeg) {
        this(new SimsmeGuid(GUID_PREFIX, UUID.randomUUID()), name, imageBase64Jpeg);
    }

    public SimsmeAccount(final SimsmeGuid guid, final String name, final String imageBase64Jpeg) {
        notNull(guid, "Argument 'guid' must not be null");
        hasText(name, "Argument 'name' must be neither null nor blank");
        this.guid = guid;
        this.name = name;
        this.imageBase64Jpeg = imageBase64Jpeg;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SimsmeAccount that = (SimsmeAccount) o;

        return guid.equals(that.guid) && !(imageBase64Jpeg != null ? !imageBase64Jpeg.equals(that.imageBase64Jpeg) : that.imageBase64Jpeg != null) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = guid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (imageBase64Jpeg != null ? imageBase64Jpeg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimsmeAccount[" +
                "guid:" + guid +
                "|name:'" + name + '\'' +
                "|imageBase64Jpeg:'" + imageBase64Jpeg + '\'' +
                ']';
    }
}
