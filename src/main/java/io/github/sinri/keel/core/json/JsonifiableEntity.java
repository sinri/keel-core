package io.github.sinri.keel.core.json;

import io.github.sinri.keel.core.SelfInterface;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.shareddata.ClusterSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * The JsonifiableEntity interface defines a contract for entities that can be
 * converted to and from a JSON object.
 * It extends the UnmodifiableJsonifiableEntity and ClusterSerializable
 * interfaces, providing additional methods
 * for working with JSON data and buffer serialization.
 *
 * <p>
 * This interface is designed to work seamlessly with JSON objects, allowing for
 * easy conversion between
 * the entity and its JSON representation. It also provides methods for reading
 * specific values from the JSON
 * object and for serializing and deserializing the entity to and from a buffer.
 *
 * <p>
 * As of 2.8:
 * extended ClusterSerializable for EventBus Messaging;
 * extended Shareable for LocalMap;
 * extended Iterable for FOREACH.
 * <p>
 * As of 4.0.12, extends {@link SelfInterface}.
 * <p>
 * As of 4.1.1, if you want to avoid generic type, you can use {@link JsonifiableDataUnit} instead.
 *
 * @param <E> the type of the entity implementing this interface
 * @since 1.14
 * @deprecated since 4.1.5, use {@link JsonifiableDataUnit} instead.
 */
@Deprecated(since = "4.1.5")
public interface JsonifiableEntity<E>
        extends JsonObjectConvertible, JsonObjectReloadable, JsonObjectWritable,
        UnmodifiableJsonifiableEntity, ClusterSerializable, SelfInterface<E> {

    /**
     * @return The JSON Object expression.
     * @since 4.1.0
     */
    default String toJsonExpression() {
        return toJsonObject().encode();
    }

    /**
     * @return The JSON Object expression which is formatted with intends.
     * @since 4.1.1
     */
    @Override
    default String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    /**
     * Reloads the data of this entity from the provided {@link JsonObject}.
     * <p>As of 4.1.1, use {@link JsonObjectReloadable#reloadData(JsonObject)} is recommended.
     *
     * @param jsonObject a non-null {@link JsonObject} containing the new data to be
     *                   loaded into the entity
     * @return the current instance of the entity, updated with the new data
     * @since 1.14
     */
    @Nonnull
    default E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        reloadData(jsonObject);
        return getImplementation();
    }


    /**
     * Reads a value from the JSON representation of this entity, using the provided
     * function to determine the path and
     * type.
     *
     * <p>
     * As of 2.8, If java.lang.ClassCastException occurred, return null instead.
     * </p>
     * <p>
     * As of 3.1.10, moved here from UnmodifiableJsonifiableEntity.
     * </p>
     *
     * @param <T>  the type of the value to be read
     * @param func a function that takes a {@link JsonPointer} and returns the class
     *             type of the value to be read
     * @return the value at the specified JSON path, cast to the specified type, or
     *         null if the value is not found or
     *         cannot be cast
     * @since 2.7
     */
    @Nullable
    default <T> T read(@Nonnull Function<JsonPointer, Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(toJsonObject());
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }

    /**
     * @param buffer the buffer to write to, should not be null
     * @since 2.8
     */
    @Override
    default void writeToBuffer(Buffer buffer) {
        JsonObject jsonObject = this.toJsonObject();
        jsonObject.writeToBuffer(buffer);
    }

    /**
     * @param pos    the position to read from, should be non-negative
     * @param buffer the buffer to read from, should not be null
     * @since 2.8
     */
    default int readFromBuffer(int pos, Buffer buffer) {
        JsonObject jsonObject = new JsonObject();
        int i = jsonObject.readFromBuffer(pos, buffer);
        this.reloadData(jsonObject);
        return i;
    }

    /**
     * @since 3.1.10 moved here from UnmodifiableJsonifiableEntity
     */
    @Override
    @Nonnull
    default Iterator<Map.Entry<String, Object>> iterator() {
        return toJsonObject().iterator();
    }

    /**
     * @since 3.1.10
     */
    @Override
    default boolean isEmpty() {
        return toJsonObject().isEmpty();
    }

    @Override
    default void ensureEntry(String key, Object value) {
        this.toJsonObject().put(key, value);
    }

    /**
     * Create or replace the Key-Value pair in this class wrapped JSON Object.
     *
     * @since 4.0.12
     */
    default E write(String key, Object value) {
        this.ensureEntry(key, value);
        return getImplementation();
    }

    /**
     * @since 4.1.1
     */
    @Override
    default void removeEntry(String key) {
        this.toJsonObject().remove(key);
    }
}
