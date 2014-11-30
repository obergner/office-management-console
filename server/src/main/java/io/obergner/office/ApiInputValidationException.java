package io.obergner.office;

import org.springframework.http.HttpStatus;

public class ApiInputValidationException extends IllegalArgumentException {

    private final String code;

    private final String bean;

    private final String field;

    private final Object rejectedValue;

    public ApiInputValidationException(final String code,
                                       final String bean,
                                       final String field,
                                       final String rejectedValue) {
        this(code, bean, field, rejectedValue, null);
    }

    public ApiInputValidationException(final String code,
                                       final String bean,
                                       final String field,
                                       final String rejectedValue,
                                       final Throwable cause) {
        super("Invalid user input: ['" + rejectedValue + "'] is not a valid value for field ['" + field + "'] in bean ['" + bean + "']", cause);
        this.code = code;
        this.bean = bean;
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public ApiValidationError asApiValidationError() {
        final ApiValidationError.FieldValidationError fieldValidationError = new ApiValidationError.FieldValidationError(this.field, getMessage());
        return new ApiValidationError(HttpStatus.BAD_REQUEST, this.code, getMessage(), new ApiValidationError.FieldValidationError[]{fieldValidationError});
    }
}
