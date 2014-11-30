package io.obergner.office;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiErrorHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MessageSource errorMessages;

    public ApiErrorHandler(final MessageSource errorMessages) {
        Assert.notNull(errorMessages);
        this.errorMessages = errorMessages;
    }

    @ExceptionHandler(JedisDataException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleDataAccessException(final JedisDataException jedisDataException) {
        this.log.warn("The server encountered an error while trying to access Redis data store: " + jedisDataException.getMessage(), jedisDataException);
        final ApiError apiError = ApiErrorCode.map(jedisDataException);
        return new ResponseEntity<>(apiError, apiError.httpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiValidationError handleApiValidationException(final MethodArgumentNotValidException manve) {
        this.log.warn("The server encountered a malformed request and failed to process it: " + manve.getMessage(), manve);
        return from(manve);
    }

    private ApiValidationError from(final MethodArgumentNotValidException manve) {
        final List<ApiValidationError.FieldValidationError> fieldValidationErrors = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiValidationError.FieldValidationError(fieldError.getField(), resolveFieldErrorMessage(fieldError)))
                .collect(Collectors.toList());
        return new ApiValidationError(HttpStatus.BAD_REQUEST,
                ApiErrorCode.MALFORMED_REQUEST,
                "The server encountered a malformed request and was not able to process it",
                fieldValidationErrors);
    }

    private String resolveFieldErrorMessage(final FieldError fieldError) {
        final Locale currentLocale = LocaleContextHolder.getLocale();
        return this.errorMessages.getMessage(fieldError.getDefaultMessage().substring(1, fieldError.getDefaultMessage().length() - 1),
                fieldError.getArguments(),
                fieldError.getDefaultMessage(),
                currentLocale);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleHttpMessageNotReadableException(final HttpMessageNotReadableException aive) {
        return handleGenericMalformedRequest(aive);
    }

    private JsonMappingException extractJsonMappingExceptionFrom(final HttpMessageNotReadableException hmnre) {
        Throwable cause = hmnre.getCause();
        while (cause != null) {
            if (cause instanceof JsonMappingException) {
                return (JsonMappingException) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    private ApiValidationError mapToApiValidationError(final JsonMappingException jme) {
        return null;
    }

    @ExceptionHandler(ApiInputValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleApiInputValidationException(final ApiInputValidationException aive) {
        this.log.warn("The server encountered a malformed request and failed to process it: " + aive.getMessage(), aive);
        return aive.asApiValidationError();
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleTypeMismatchException(final TypeMismatchException tme) {
        return handleGenericMalformedRequest(tme);
    }

    private ApiError handleGenericMalformedRequest(final Exception exception) {
        this.log.warn("The server encountered a malformed request and failed to process it: " + exception.getMessage(), exception);
        return new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCode.MALFORMED_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleIllegalArgumentException(final IllegalArgumentException iae) {
        return handleGenericMalformedRequest(iae);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handleUnexpectedException(final Exception unexpectedException) {
        this.log.error("Unexpected exception caught: " + unexpectedException.getMessage(), unexpectedException);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_SERVER_ERROR,
                "The server encountered an unexpected exception while processing your request: " + unexpectedException.getMessage());
    }
}
