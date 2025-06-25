package io.github.sinri.keel.core.json;

import io.github.sinri.keel.core.SelfInterface;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.shareddata.ClusterSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
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
 * </p>
 * <p>
 * As of 4.0.12, extends {@link SelfInterface}.
 * </p>
 *
 * @param <E> the type of the entity implementing this interface
 * @since 1.14
 */
public interface JsonifiableEntity<E>
        extends UnmodifiableJsonifiableEntity, ClusterSerializable, SelfInterface<E> {

    /**
     * Wrap a JsonObject into a JsonifiableEntity.
     *
     * @param jsonObject The JsonObject to wrap.
     * @return A JsonifiableEntity wraps the JsonObject.
     * @since 3.2.11
     * @deprecated As of 4.0.14, {@link SimpleJsonifiableEntity} is deprecated. For read-only scenario, use
     *         {@link UnmodifiableJsonifiableEntity#wrap(JsonObject)}.
     */
    @Deprecated(since = "4.0.14")
    static SimpleJsonifiableEntity wrap(@Nonnull JsonObject jsonObject) {
        return new SimpleJsonifiableEntity(jsonObject);
    }

    /**
     * Converts the current state of this entity into a {@link JsonObject}.
     *
     * @return a non-null {@link JsonObject} representing the current state of the
     *         entity.
     * @since 1.14
     */
    @Nonnull
    JsonObject toJsonObject();

    /**
     * @return The JSON Object expression.
     * @since 4.0.14
     */
    default String toJsonExpression() {
        return toJsonObject().toString();
    }

    /**
     * Reloads the data of this entity from the provided {@link JsonObject}.
     *
     * @param jsonObject a non-null {@link JsonObject} containing the new data to be
     *                   loaded into the entity
     * @return the current instance of the entity, updated with the new data
     * @since 1.14
     */
    @Nonnull
    E reloadDataFromJsonObject(@Nonnull JsonObject jsonObject);

    /**
     * It should be the same as {@link JsonifiableEntity#toJsonExpression()}.
     *
     * @return The JsonObject expression.
     * @since 1.14
     */
    @Override
    String toString();

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
     * Attempts to read and convert a JSON object into an instance of the specified
     * class that implements
     * {@link JsonifiableEntity}.
     *
     * <p>
     * This method first tries to create an instance of the provided class using its
     * no-argument constructor and
     * then
     * loads the data from the JSON object. If the no-argument constructor is not
     * available, it tries to use a
     * constructor
     * that takes a {@link JsonObject} as an argument. If neither constructor is
     * found or an exception occurs during
     * instantiation, the method returns null.
     * </p>
     *
     * @param bClass the class of the entity to be created, which must extend or
     *               implement {@link JsonifiableEntity}
     * @param args   the arguments used to form a JSON pointer for locating the JSON
     *               object within a larger structure
     * @param <B>    the type of the entity to be created
     * @return an instance of the specified class with data loaded from the JSON
     *         object, or null if the operation fails
     * @since 2.7
     */
    default @Nullable <B extends JsonifiableEntity<?>> B readJsonifiableEntity(@Nonnull Class<B> bClass,
                                                                               String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null)
            return null;
        try {
            var x = bClass.getConstructor().newInstance();
            x.reloadDataFromJsonObject(jsonObject);
            return x;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException ignored1) {
            try {
                return bClass.getConstructor(JsonObject.class).newInstance(jsonObject);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                     | NoSuchMethodException ignored2) {
                return null;
            }
        }
    }

    /**
     * Read an entity from a JSON Object with Jackson
     * {@link JsonObject#mapTo(Class)}.
     *
     * @param cClass The class of the entity to be read.
     * @param args   The arguments used to form a JSON pointer for locating the JSON
     *               object within a larger structure.
     * @param <C>    The type of the entity to be read.
     * @return The entity read from the JSON Object.
     * @since 4.0.13
     */
    default @Nullable <C> C readEntity(@Nonnull Class<C> cClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null) {
            return null;
        }
        try {
            return jsonObject.mapTo(cClass);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @since 2.8
     */
    default void fromBuffer(@Nonnull Buffer buffer) {
        this.reloadDataFromJsonObject(new JsonObject(buffer));
    }

    /**
     * @param buffer the buffer to write to, should not be null
     * @since 2.8
     */
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
        this.reloadDataFromJsonObject(jsonObject);
        return i;
    }

    /**
     * As of 3.1.10, moved here from UnmodifiableJsonifiableEntity.
     *
     * @since 2.8
     */
    default Buffer toBuffer() {
        return toJsonObject().toBuffer();
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

    /**
     * Retrieves the JSON object associated with the specified key from the entity's
     * JSON representation.
     * If no JSON object exists for the given key, a new empty JSON object is
     * created, associated with the key,
     * and added to the entity's JSON representation. This ensures that the key
     * always maps to a valid JSON object.
     *
     * @param key the key for which the JSON object is to be retrieved or created
     * @return the existing or newly created JSON object associated with the
     *         specified key
     * @since 4.0.12
     */
    default JsonObject ensureJsonObject(String key) {
        JsonObject x = this.readJsonObject(key);
        if (x == null) {
            x = new JsonObject();
            this.toJsonObject().put(key, x);
        }
        return x;
    }

    /**
     * Retrieves the JSON array associated with the specified key from the entity's
     * JSON representation.
     * If no JSON array exists for the given key, a new empty JSON array is created,
     * associated with the key,
     * and added to the entity's JSON representation. This ensures that the key
     * always maps to a valid JSON array.
     *
     * @param key the key for which the JSON array is to be retrieved or created
     * @return the existing or newly created JSON array associated with the
     *         specified key
     * @since 4.0.12
     */
    default JsonArray ensureJsonArray(String key) {
        JsonArray x = this.readJsonArray(key);
        if (x == null) {
            x = new JsonArray();
            this.toJsonObject().put(key, x);
        }
        return x;
    }

    /**
     * Create or replace the Key-Value pair in this class wrapped JSON Object.
     *
     * @since 4.0.12
     */
    default E write(String key, Object value) {
        this.toJsonObject().put(key, value);
        return getImplementation();
    }
}
