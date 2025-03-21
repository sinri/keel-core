package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;
import io.vertx.core.shareddata.Shareable;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A simple implementation of the {@link JsonifiableEntity} interface that provides basic functionality for
 * converting an entity to and from a JSON object. This class can be extended by other classes to add specific
 * properties and methods while maintaining the ability to work with JSON data.
 *
 * <p>As of 2.8, this class allows the initialization of the internal JSON object to be null, which is treated as an
 * empty JSON object. It also implements additional interfaces such as {@link ClusterSerializable}, {@link Shareable},
 * and {@link Iterable} to support more advanced use cases.</p>
 *
 * @since 2.7
 */
public class SimpleJsonifiableEntity implements JsonifiableEntity<SimpleJsonifiableEntity> {
    @Nonnull
    protected JsonObject jsonObject;

    /**
     * Constructs a new instance of {@code SimpleJsonifiableEntity} and initializes the internal JSON object to an
     * empty
     * {@link JsonObject}.
     *
     * <p>This constructor is useful when creating a new entity without any initial data. The internal JSON object will
     * be
     * created and can be populated with data using other methods provided by this class.</p>
     */
    public SimpleJsonifiableEntity() {
        this.jsonObject = new JsonObject();
    }

    /**
     * Constructs a new instance of {@code SimpleJsonifiableEntity} and initializes the internal JSON object with the
     * provided
     * {@link JsonObject}.
     *
     * <p>This constructor is useful when creating an entity with pre-existing data. The internal JSON object will be
     * initialized with the data from the provided {@link JsonObject}.</p>
     *
     * @param jsonObject a non-null {@link JsonObject} containing the initial data for the entity
     */
    public SimpleJsonifiableEntity(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Converts the current state of this entity into a {@link JsonObject}.
     *
     * @return a non-null {@link JsonObject} representing the current state of the entity
     */
    @Override
    public @Nonnull JsonObject toJsonObject() {
        return jsonObject;
    }

    /**
     * @since 2.8 allow jsonObject as null (treated as empty json object)
     */
    @Override
    public @Nonnull SimpleJsonifiableEntity reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        this.jsonObject = Objects.requireNonNullElseGet(jsonObject, JsonObject::new);
        return this;
    }

    /**
     * @since 2.8
     */
    @Override
    public String toString() {
        return toJsonObject().toString();
    }

    @Override
    public SimpleJsonifiableEntity copy() {
        return new SimpleJsonifiableEntity(cloneAsJsonObject());
    }
}
