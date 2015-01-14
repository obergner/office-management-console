package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateNewSimsmeAccountRefCreation extends SimsmeAccountRefModification {

    private static final long serialVersionUID = 5564700480889068139L;

    @JsonProperty(value = "name", required = false)
    public final String name;

    @JsonProperty(value = "imageBase64Jpeg", required = false)
    public final String imageBase64Jpeg;

    @JsonCreator
    public CreateNewSimsmeAccountRefCreation(final @JsonProperty("name") String name,
                                             final @JsonProperty("imageBase64Jpeg") String imageBase64Jpeg) {
        super(Action.createNew);
        this.name = name;
        this.imageBase64Jpeg = imageBase64Jpeg;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CreateNewSimsmeAccountRefCreation that = (CreateNewSimsmeAccountRefCreation) o;

        return !(imageBase64Jpeg != null ? !imageBase64Jpeg.equals(that.imageBase64Jpeg) : that.imageBase64Jpeg != null) && !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (imageBase64Jpeg != null ? imageBase64Jpeg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CreateNewSimsmeAccountRefCreation[" +
                "name:'" + name + '\'' +
                "|imageBase64Jpeg:'" + imageBase64Jpeg + '\'' +
                ']';
    }
}
