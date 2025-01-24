package io.github.sinri.keel.test.lab.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class JsonTest2 extends KeelInstantRunner {
    @InstantRunUnit
    public Future<Void> test() {
        var j = new JsonObject()
                .put("name", "asdf")
                .put("privateName", "privateName")
                .put("protectedName", "protectedName")
                .put("special_value", "sv");

        VO vo = Json.decodeValue(j.toString(), VO.class);

        getLogger().info("vo.name = " + vo.name);
        getLogger().info("vo.value = " + vo.value);

        return Future.succeededFuture();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VO {
        public String name;
        @JsonProperty("special_value")
        public String value;
        protected String protectedName;
        private String privateName;
    }
}
