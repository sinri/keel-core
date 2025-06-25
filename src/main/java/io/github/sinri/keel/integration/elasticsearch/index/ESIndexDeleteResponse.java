package io.github.sinri.keel.integration.elasticsearch.index;

import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;

public class ESIndexDeleteResponse extends UnmodifiableJsonifiableEntityImpl {
    public ESIndexDeleteResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
