package io.obergner.office;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiValidationError extends ApiError {

    @JsonProperty("fieldValidationErrors")
    public final FieldValidationError[] fieldValidationErrors;

    public ApiValidationError(final HttpStatus status,
                              final String code,
                              final String message,
                              final FieldValidationError[] fieldValidationErrors) {
        super(status, code, message);
        this.fieldValidationErrors = fieldValidationErrors;
    }

    public ApiValidationError(final HttpStatus status,
                              final String code,
                              final String message,
                              final List<FieldValidationError> fieldValidationErrors) {
        this(status, code, message, fieldValidationErrors.toArray(new FieldValidationError[fieldValidationErrors.size()]));
    }

    @JsonCreator
    public ApiValidationError(final @JsonProperty("status") int rawStatus,
                              final @JsonProperty("code") String code,
                              final @JsonProperty("message") String message,
                              final @JsonProperty("fieldValidationErrors") FieldValidationError[] fieldValidationErrors) {
        this(HttpStatus.valueOf(rawStatus), code, message, fieldValidationErrors);
    }

    public static class FieldValidationError {

        @JsonProperty("field")
        public final String field;

        @JsonProperty("message")
        public final String message;

        @JsonCreator
        public FieldValidationError(final String field, final String message) {
            this.field = field;
            this.message = message;
        }

        @Override
        public String toString() {
            return "FieldValidationError{" +
                    "field='" + field + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
