package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 4.0.12
 */
public abstract class JsonifiableEntityImpl<E> implements JsonifiableEntity<E> {
    private JsonObject jsonObject;

    public JsonifiableEntityImpl(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JsonifiableEntityImpl() {
        this(new JsonObject());
    }

    @Nonnull
    @Override
    public final JsonObject toJsonObject() {
        return jsonObject;
    }

    @Nonnull
    @Override
    public final E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        return this.getImplementation();
    }

    @Override
    public final String toJsonExpression() {
        return this.toJsonObject().toString();
    }

    @Override
    public final String toString() {
        return this.toJsonExpression();
    }

}
