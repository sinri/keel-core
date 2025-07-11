package io.github.sinri.keel.web.http.receptionist.responder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any exception thrown from receptionists to responder should be and recommended to be instance-of this class;
 * or would be automatically wrapped to this class.
 *
 * @since 4.1.0
 */
public class KeelWebApiError extends Exception {
    private final int statusCode;

    public KeelWebApiError(@Nonnull String message) {
        this(200, message, null);
    }

    public KeelWebApiError(@Nonnull String message, @Nullable Throwable cause) {
        this(200, message, cause);
    }

    public KeelWebApiError(int statusCode, @Nonnull String message, @Nullable Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public static KeelWebApiError wrap(@Nonnull Throwable throwable) {
        return wrap(200, throwable);
    }

    public static KeelWebApiError wrap(int statusCode, @Nonnull Throwable throwable) {
        return new KeelWebApiError(statusCode, "Web API Error with message: " + throwable.getMessage(), throwable);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
