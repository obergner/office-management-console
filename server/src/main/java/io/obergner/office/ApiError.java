package io.obergner.office;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public class ApiError {

    @JsonProperty("status")
    public final int status;

    @JsonProperty("code")
    public final String code;

    @JsonProperty("message")
    public final String message;

    @JsonCreator
    public ApiError(final @JsonProperty("status") int status,
                    final @JsonProperty("code") String code,
                    final @JsonProperty("message") String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ApiError(final HttpStatus status, final String code, final String message) {
        this(status.value(), code, message);
    }

    public HttpStatus httpStatus() {
        return HttpStatus.valueOf(this.status);
    }

    @Override
    public String toString() {
        return "ApiError[" +
                "status:" + status +
                "|code:'" + code + '\'' +
                "|message:'" + message + '\'' +
                ']';
    }
}
