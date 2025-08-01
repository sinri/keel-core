package io.github.sinri.keel.core.json;

/**
 * An interface for those entities could be serialized as a string of JSON Expression.
 *
 * @since 4.1.1
 */
public interface JsonSerializable {
    String toJsonExpression();

    String toFormattedJsonExpression();

    /**
     * Should return same content with {@link JsonSerializable#toJsonExpression()}
     *
     * @return The JSON Object expression.
     */
    @Override
    String toString();


}
