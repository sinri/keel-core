package io.github.sinri.keel.web.http.receptionist.responder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any exception thrown from receptionists to responder should be and recommended to be instance-of this class;
 * or would be automatically wrapped to this class.
 *
 * @since 4.1.0
 */
public class KeelWebApiError extends RuntimeException {
    private final int statusCode;

    protected KeelWebApiError(@Nonnull String message) {
        this(200, message, null);
    }

    protected KeelWebApiError(@Nonnull String message, @Nullable Throwable cause) {
        this(200, message, cause);
    }

    protected KeelWebApiError(int statusCode, @Nonnull String message, @Nullable Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Throws a new {@link KeelWebApiError} with the specified status code, message, and cause.
     *
     * @param statusCode the HTTP status code associated with the error
     * @param message    the detail message for the error (must not be null)
     * @param throwable  the cause of the error (must not be null)
     */
    public static void issue(int statusCode, @Nonnull String message, @Nullable Throwable throwable) {
        throw new KeelWebApiError(statusCode, message, throwable);
    }

    /**
     * Throws a new {@link KeelWebApiError} with a default status code of 200, the specified message, and no cause.
     *
     * @param message the detail message for the error (must not be null)
     */
    public static void issue(@Nonnull String message) {
        issue(200, message, null);
    }

    /**
     * Throws a new {@link KeelWebApiError} with a default status code of 200, a message constructed from the given
     * throwable, and the throwable itself as the cause.
     *
     * @param throwable the cause of the error (must not be null)
     */
    public static void issue(@Nonnull Throwable throwable) {
        throw wrap(throwable);
    }

    static KeelWebApiError wrap(Throwable throwable) {
        return new KeelWebApiError(200, "Web API Error with message: " + throwable.getMessage(), throwable);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
