package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;
import io.vertx.core.shareddata.Shareable;

import javax.annotation.Nonnull;

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
 * @deprecated as of 4.0.14, this class is not recommended to use, define a detailed implement class instead.
 */
@Deprecated(since = "4.0.14")
public class SimpleJsonifiableEntity extends JsonifiableEntityImpl<SimpleJsonifiableEntity> {

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
        super();
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
        super(jsonObject);
    }

    @Override
    public SimpleJsonifiableEntity copy() {
        return new SimpleJsonifiableEntity(cloneAsJsonObject());
    }

    @Nonnull
    @Override
    public SimpleJsonifiableEntity getImplementation() {
        return this;
    }
}
