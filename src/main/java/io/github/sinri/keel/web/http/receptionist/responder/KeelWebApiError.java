package io.github.sinri.keel.web.http.receptionist.responder;

import javax.annotation.Nonnull;

/**
 * Any exception thrown from receptionists to responder should be and recommended to be instance-of this class;
 * or would be automatically wrapped to this class.
 *
 * @since 4.1.0
 */
public class KeelWebApiError extends Exception {
    public KeelWebApiError(@Nonnull String message) {
        super(message);
    }

    public KeelWebApiError(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public static KeelWebApiError wrap(@Nonnull Throwable throwable) {
        return new KeelWebApiError("Web API Error with message: " + throwable.getMessage(), throwable);
    }
}
