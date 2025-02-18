package io.github.sinri.keel.web.http.requester.error;

import io.vertx.core.buffer.Buffer;

import javax.annotation.Nullable;

/**
 * @since 4.0.3
 */
public final class ReceivedUnexpectedFormatResponse extends ReceivedUnexpectedResponse {
    public ReceivedUnexpectedFormatResponse(int responseStatusCode, @Nullable Buffer responseBody) {
        super("received response with dody in unexpected format", responseStatusCode, responseBody);
    }
}
