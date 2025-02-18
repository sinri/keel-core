package io.github.sinri.keel.web.http.requester.extractor;

import io.github.sinri.keel.web.http.requester.error.ReceivedAbnormalStatusResponse;
import io.github.sinri.keel.web.http.requester.error.ReceivedUnexpectedFormatResponse;
import io.github.sinri.keel.web.http.requester.error.ReceivedUnexpectedResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * To filter out the situation that Response Status Code is set and not 200, and the response body is not in JSON object
 * format.
 *
 * @since 4.0.3
 */
public class KeelWebResponseExtractorOnJsonObjectFormat extends KeelWebResponseExtractor<JsonObject> {
    public KeelWebResponseExtractorOnJsonObjectFormat(HttpResponse<Buffer> response) {
        super(response);
    }

    public KeelWebResponseExtractorOnJsonObjectFormat(int responseStatusCode, @Nullable Buffer responseBody) {
        super(responseStatusCode, responseBody);
    }

    @Override
    @Nonnull
    public JsonObject extract() throws ReceivedUnexpectedResponse {
        var responseStatusCode = getResponseStatusCode();
        var responseBody = this.getResponseBody();

        if (responseStatusCode != 200) {
            throw new ReceivedAbnormalStatusResponse(responseStatusCode, responseBody);
        }

        try {
            Objects.requireNonNull(responseBody);
            return responseBody.toJsonObject();
        } catch (Exception e) {
            throw new ReceivedUnexpectedFormatResponse(responseStatusCode, responseBody);
        }
    }
}
