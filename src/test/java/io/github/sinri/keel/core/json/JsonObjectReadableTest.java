package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class JsonObjectReadableTest extends KeelJUnit5Test {

    public JsonObjectReadableTest(Vertx vertx) {
        super(vertx);
    }

}