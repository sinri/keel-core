package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.core.json.JsonifiedThrowable;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 3.1.10
 */
public class KeelIssueRecordJsonObjectRender implements KeelIssueRecordRender<JsonObject> {
    private static final KeelIssueRecordJsonObjectRender instance = new KeelIssueRecordJsonObjectRender();

    protected KeelIssueRecordJsonObjectRender() {

    }

    public static KeelIssueRecordJsonObjectRender getInstance() {
        return instance;
    }

    @Nonnull
    @Override
    public JsonObject renderIssueRecord(@Nonnull String topic, @Nonnull KeelIssueRecord<?> issueRecord) {
        // by default, the topic is neglected for Aliyun SLS eco

        JsonObject x = new JsonObject();

        x.put("level", issueRecord.level());

        JsonArray classification = new JsonArray();
        issueRecord.classification().forEach(classification::add);
        x.put("classification", classification);

        issueRecord.attributes().forEach(entry -> x.put(entry.getKey(), entry.getValue()));
        Throwable exception = issueRecord.exception();
        if (exception != null) {
            x.put("exception", renderThrowable(exception));
        }
        return x;
    }

    /**
     * A JSON CODEC REGISTER action is required, i.e.
     * {@link JsonifiableSerializer#register()}, to ensure this class to work correctly.
     */
    @Nonnull
    @Override
    public JsonObject renderThrowable(@Nonnull Throwable throwable) {
        return JsonifiedThrowable.wrap(throwable, ignorableStackPackageSet(), true).toJsonObject();
        //return Objects.requireNonNull(Keel.jsonHelper().renderThrowableChain(throwable, ignorableStackPackageSet()));
    }
}
