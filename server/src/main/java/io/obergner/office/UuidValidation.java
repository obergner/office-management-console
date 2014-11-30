package io.obergner.office;

import java.util.UUID;

public final class UuidValidation {

    public static UUID checkAndConvert(final String code, final String bean, final String field, final String uuidStr) {
        if (uuidStr == null) {
            throw new ApiInputValidationException(code, bean, field, uuidStr);
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (final IllegalArgumentException iae) {
            throw new ApiInputValidationException(code, bean, field, uuidStr, iae);
        }
    }
}
