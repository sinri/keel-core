package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

class JsonifiableSerializerTest extends KeelUnitTest {

    @BeforeAll
    static void prepare() {
        JsonifiableSerializer.register();
    }

    @Test
    void serialize() {
        Sth sth = new Sth();
        sth.write("a", "b");
        sth.write("b", Map.of("p", "q"));
        sth.write("c", List.of("a", "b", "c", "d"));

        JsonObject j = new JsonObject();
        j.put("sth", sth);
        System.out.println(j);
    }

    private static class Sth extends JsonifiableEntityImpl<Sth> {

        @Nonnull
        @Override
        public Sth getImplementation() {
            return this;
        }
    }
}