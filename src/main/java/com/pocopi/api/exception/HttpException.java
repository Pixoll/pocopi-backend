package com.pocopi.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@SuppressWarnings("unused")
@Getter
public class HttpException extends ApiException {
    private final HttpStatus status;

    public HttpException(HttpStatus status, String message) {
        super(message);

        this.status = status;
    }

    public static HttpException badRequest(String message) {
        return new HttpException(HttpStatus.BAD_REQUEST, message);
    }

    public static HttpException unauthorized(String message) {
        return new HttpException(HttpStatus.UNAUTHORIZED, message);
    }

    public static HttpException forbidden(String message) {
        return new HttpException(HttpStatus.FORBIDDEN, message);
    }

    public static HttpException notFound(String message) {
        return new HttpException(HttpStatus.NOT_FOUND, message);
    }

    public static HttpException requestTimeout(String message) {
        return new HttpException(HttpStatus.REQUEST_TIMEOUT, message);
    }

    public static HttpException conflict(String message) {
        return new HttpException(HttpStatus.CONFLICT, message);
    }

    public static HttpException payloadTooLarge(String message) {
        return new HttpException(HttpStatus.PAYLOAD_TOO_LARGE, message);
    }

    public static HttpException unsupportedMediaType(String message) {
        return new HttpException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    public static HttpException unprocessableEntity(String message) {
        return new HttpException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static HttpException tooManyRequests(String message) {
        return new HttpException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    public static HttpException internalServerError(String message) {
        return new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static HttpException internalServerError(Throwable cause) {
        return new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, cause.getMessage());
    }

    public static HttpException internalServerError(String message, Throwable cause) {
        return new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, message + ": " + cause.getMessage());
    }
}
