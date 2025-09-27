package io.github.sinri.keel.core.json;

/**
 * An interface for those entities could be serialized as a string of JSON Expression.
 *
 * @since 4.1.1
 */
public interface JsonSerializable {
    /**
     * @return the JSON Object expression of this instance.
     */
    String toJsonExpression();

    /**
     * @return the formatted JSON Object expression of this instance.
     */
    String toFormattedJsonExpression();

    /**
     * Should return same content with {@link JsonSerializable#toJsonExpression()}
     *
     * @return The JSON Object expression.
     * @see JsonSerializable#toJsonExpression()
     * @deprecated do not rely on this method to retrieve JSON expression, use
     *         {@link JsonSerializable#toJsonExpression()} instead to avoid risks.
     */
    @Deprecated(since = "4.1.5")
    @Override
    String toString();


}
