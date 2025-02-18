package io.github.sinri.keel.web.http.requester.extractor;

import io.github.sinri.keel.web.http.requester.error.ReceivedUnexpectedResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;

import javax.annotation.Nullable;

/**
 * Validate the response of web requests, and extract the content if validated.
 *
 * @since 4.0.3
 */
public abstract class KeelWebResponseExtractor<T> {
    private final int responseStatusCode;
    private final @Nullable Buffer responseBody;
    private @Nullable HttpMethod requestMethod;
    private @Nullable String requestTarget;
    private @Nullable Buffer requestBody;

    public KeelWebResponseExtractor(HttpResponse<Buffer> response) {
        this(response.statusCode(), response.body());
    }

    public KeelWebResponseExtractor(int responseStatusCode, @Nullable Buffer responseBody) {
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }


    @Nullable
    public Buffer getRequestBody() {
        return requestBody;
    }


    @Nullable
    public Buffer getResponseBody() {
        return responseBody;
    }

    @Nullable
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    public KeelWebResponseExtractor<T> setRequestMethod(@Nullable HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    @Nullable
    public String getRequestTarget() {
        return requestTarget;
    }

    public KeelWebResponseExtractor<T> setRequestTarget(@Nullable String requestTarget) {
        this.requestTarget = requestTarget;
        return this;
    }

    public abstract T extract() throws ReceivedUnexpectedResponse;
}
