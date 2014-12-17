package io.obergner.office;

import org.springframework.http.HttpStatus;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class ApiErrorCode {

    // Public only for testing purposes
    public static final String INTERNAL_SERVER_ERROR = "api.error.common.internal-server-error";

    public static final String MALFORMED_REQUEST = "api.error.common.malformed-request";

    public static final String NO_ACCOUNT_WITH_MATCHING_MMAID_FOUND = "api.error.account.no-account-with-matching-mmaid-found";

    public static final String NO_ACCOUNT_WITH_MATCHING_UUID_FOUND = "api.error.account.no-account-with-matching-uuid-found";

    public static final String DUPLICATE_ACCOUNT_MMAID = "api.error.account.duplicate-account-mma-id";

    public static final String DUPLICATE_ACCOUNT_UUID = "api.error.account.duplicate-account-uuid";

    public static final String INCONSISTENT_ACCOUNT_DATA_FOUND = "api.error.account.inconsistent-data-found";

    private static final ApiErrorCode FALLBACK = new ApiErrorCode(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

    private static final String UNKNOWN_ERROR = "UNKNOWN ERROR";

    private static final Set<ApiErrorCode> ALL_API_ERROR_CODES;

    static {
        final Set<ApiErrorCode> tmp = new HashSet<>(20);
        tmp.add(FALLBACK);
        tmp.add(new ApiErrorCode(MALFORMED_REQUEST, HttpStatus.BAD_REQUEST));
        tmp.add(new ApiErrorCode(NO_ACCOUNT_WITH_MATCHING_MMAID_FOUND, HttpStatus.NOT_FOUND));
        tmp.add(new ApiErrorCode(NO_ACCOUNT_WITH_MATCHING_UUID_FOUND, HttpStatus.NOT_FOUND));
        tmp.add(new ApiErrorCode(DUPLICATE_ACCOUNT_MMAID, HttpStatus.CONFLICT));
        tmp.add(new ApiErrorCode(DUPLICATE_ACCOUNT_UUID, HttpStatus.CONFLICT));
        tmp.add(new ApiErrorCode(INCONSISTENT_ACCOUNT_DATA_FOUND, HttpStatus.CONFLICT));
        ALL_API_ERROR_CODES = Collections.unmodifiableSet(tmp);
    }

    public static ApiError map(final JedisDataException jde) {
        final String[] codeAndMessage = jde.getMessage().split(":", 2);
        final String code = codeAndMessage[0];
        final String message = codeAndMessage.length > 1 ? codeAndMessage[1] : UNKNOWN_ERROR;
        final Optional<ApiErrorCode> matchingApiErrorCodeOpt = ALL_API_ERROR_CODES.stream().filter(candidate -> candidate.code.equalsIgnoreCase(code)).findFirst();
        final ApiErrorCode matchingApiErrorCode = matchingApiErrorCodeOpt.orElse(FALLBACK);
        return new ApiError(matchingApiErrorCode.status, matchingApiErrorCode.code, message);
    }

    public final String code;

    public final HttpStatus status;

    private ApiErrorCode(final String code, final HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApiErrorCode that = (ApiErrorCode) o;

        return code.equals(that.code) && status == that.status;

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ApiErrorCode[" +
                "code:'" + code + '\'' +
                "|status:" + status +
                ']';
    }
}
