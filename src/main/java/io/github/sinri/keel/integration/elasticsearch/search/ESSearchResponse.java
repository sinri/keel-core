package io.github.sinri.keel.integration.elasticsearch.search;

import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;

public class ESSearchResponse extends UnmodifiableJsonifiableEntityImpl {
    public ESSearchResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
