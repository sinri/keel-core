package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@ExtendWith(VertxExtension.class)
class JsonifiableSerializerTest extends KeelJUnit5Test {

    public JsonifiableSerializerTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeAll
    static void prepare() {
        JsonifiableSerializer.register();
    }

    @Test
    void serialize() {
        Sth sth = new Sth();
        sth.ensureEntry("a", "b");
        sth.ensureEntry("b", Map.of("p", "q"));
        sth.ensureEntry("c", List.of("a", "b", "c", "d"));

        JsonObject j = new JsonObject();
        j.put("sth", sth);
        getUnitTestLogger().info("j: "+j);
        getUnitTestLogger().info("j",j);

        var k= new JsonObject(j.toString());
        getUnitTestLogger().info("k: "+k);
        getUnitTestLogger().info("k",k);
    }

    private static class Sth extends JsonifiableEntityImpl<Sth> {

        @Nonnull
        @Override
        public Sth getImplementation() {
            return this;
        }
    }
}