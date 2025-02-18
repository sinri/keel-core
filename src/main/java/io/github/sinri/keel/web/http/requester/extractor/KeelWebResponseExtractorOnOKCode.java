package io.github.sinri.keel.web.http.requester.extractor;

import io.github.sinri.keel.web.http.requester.error.ReceivedFailedResponse;
import io.github.sinri.keel.web.http.requester.error.ReceivedUnexpectedResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * To filter out the situation that Response Status Code is set and not 200, and the response body is not in JSON object
 * format, and the value mapped to key {@code code} is not {@code OK}.
 *
 * @since 4.0.3
 */
public class KeelWebResponseExtractorOnOKCode extends KeelWebResponseExtractorOnJsonObjectFormat {
    public KeelWebResponseExtractorOnOKCode(HttpResponse<Buffer> response) {
        super(response);
    }

    public KeelWebResponseExtractorOnOKCode(int responseStatusCode, @Nullable Buffer responseBody) {
        super(responseStatusCode, responseBody);
    }

    @Nonnull
    @Override
    public JsonObject extract() throws ReceivedUnexpectedResponse {
        JsonObject j = super.extract();
        String code = j.getString("code");
        if (!Objects.equals("OK", code)) {
            throw new ReceivedFailedResponse(getResponseStatusCode(), getResponseBody());
        }
        return j;
    }
}
