package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonObjectConvertible} interface.
 * <p>
 * This test class verifies the contract and behavior of the JsonObjectConvertible interface
 * by testing a concrete implementation and ensuring all methods work correctly.
 */
@ExtendWith(VertxExtension.class)
class JsonObjectConvertibleTest extends KeelJUnit5Test {

    private TestJsonObjectConvertible emptyConvertible;
    private TestJsonObjectConvertible populatedConvertible;
    private JsonObject testJsonObject;

    public JsonObjectConvertibleTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    public void setUp() {
        // Create test data
        testJsonObject = new JsonObject()
                .put("string", "test value")
                .put("number", 42)
                .put("boolean", true)
                .put("null", null)
                .put("array", new JsonArray().add("item1").add("item2"))
                .put("object", new JsonObject().put("nested", "value"));

        emptyConvertible = new TestJsonObjectConvertible(new JsonObject());
        populatedConvertible = new TestJsonObjectConvertible(testJsonObject);
    }

    @Test
    void testToJsonObjectReturnsNonNull() {
        // Test that toJsonObject() always returns a non-null JsonObject
        JsonObject result = populatedConvertible.toJsonObject();
        assertNotNull(result, "toJsonObject() should never return null");

        JsonObject emptyResult = emptyConvertible.toJsonObject();
        assertNotNull(emptyResult, "toJsonObject() should never return null even for empty data");
    }

    @Test
    void testToJsonObjectReturnsCorrectData() {
        JsonObject result = populatedConvertible.toJsonObject();

        // Verify all fields are correctly preserved
        assertEquals("test value", result.getString("string"));
        assertEquals(42, result.getInteger("number"));
        assertTrue(result.getBoolean("boolean"));
        assertNull(result.getValue("null"));

        // Verify array and object fields
        JsonArray array = result.getJsonArray("array");
        assertNotNull(array);
        assertEquals("item1", array.getString(0));
        assertEquals("item2", array.getString(1));
        assertEquals("value", result.getJsonObject("object").getString("nested"));
    }

    @Test
    void testToJsonObjectReturnsSameInstance() {
        // Test that toJsonObject() returns the same instance when called multiple times
        JsonObject firstCall = populatedConvertible.toJsonObject();
        JsonObject secondCall = populatedConvertible.toJsonObject();

        assertSame(firstCall, secondCall, "toJsonObject() should return the same instance on multiple calls");
    }

    @Test
    void testToJsonObjectWithEmptyData() {
        JsonObject result = emptyConvertible.toJsonObject();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Empty convertible should return empty JsonObject");
        assertEquals(0, result.size());
    }

    @Test
    void testToJsonObjectWithComplexData() {
        // Create complex nested data
        JsonObject complexData = new JsonObject()
                .put("level1", new JsonObject()
                        .put("level2", new JsonObject()
                                .put("level3", "deep value")))
                .put("array", new JsonObject()
                        .put("items", new JsonArray().add("a").add("b").add("c")))
                .put("mixed", new JsonObject()
                        .put("string", "text")
                        .put("number", 123.45)
                        .put("boolean", false)
                        .put("null", null));

        TestJsonObjectConvertible complexConvertible = new TestJsonObjectConvertible(complexData);
        JsonObject result = complexConvertible.toJsonObject();

        // Verify nested structure
        assertEquals("deep value", result.getJsonObject("level1").getJsonObject("level2").getString("level3"));
        JsonArray items = result.getJsonObject("array").getJsonArray("items");
        assertEquals("a", items.getString(0));
        assertEquals("b", items.getString(1));
        assertEquals("c", items.getString(2));

        // Verify mixed types
        JsonObject mixed = result.getJsonObject("mixed");
        assertEquals("text", mixed.getString("string"));
        assertEquals(123.45, mixed.getDouble("number"), 0.001);
        assertFalse(mixed.getBoolean("boolean"));
        assertNull(mixed.getValue("null"));
    }

    @Test
    void testToJsonExpression() {
        String result = populatedConvertible.toJsonExpression();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testJsonObject.encode(), result);

        // Verify it's valid JSON by parsing it back
        JsonObject parsed = new JsonObject(result);
        assertEquals(testJsonObject.encode(), parsed.encode());
    }

    @Test
    void testToFormattedJsonExpression() {
        String result = populatedConvertible.toFormattedJsonExpression();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testJsonObject.encodePrettily(), result);

        // Verify it's valid JSON by parsing it back
        JsonObject parsed = new JsonObject(result);
        assertEquals(testJsonObject.encode(), parsed.encode());

        // Verify it contains formatting (newlines and spaces)
        assertTrue(result.contains("\n") || result.contains(" "),
                "Formatted JSON should contain formatting characters");
    }

    @Test
    void testToString() {
        String result = populatedConvertible.toString();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(populatedConvertible.toJsonExpression(), result);

        // Verify toString() returns the same as toJsonExpression()
        assertEquals(populatedConvertible.toJsonExpression(), result);
    }

    @Test
    void testEmptyConvertibleMethods() {
        // Test all methods with empty data
        JsonObject jsonObject = emptyConvertible.toJsonObject();
        String jsonExpression = emptyConvertible.toJsonExpression();
        String formattedExpression = emptyConvertible.toFormattedJsonExpression();
        String toString = emptyConvertible.toString();

        assertNotNull(jsonObject);
        assertTrue(jsonObject.isEmpty());
        assertEquals("{}", jsonExpression);
        assertTrue(formattedExpression.contains("{") && formattedExpression.contains("}"));
        assertEquals(jsonExpression, toString);
    }

    @Test
    void testConsistencyBetweenMethods() {
        // Test that all serialization methods are consistent
        JsonObject jsonObject = populatedConvertible.toJsonObject();
        String jsonExpression = populatedConvertible.toJsonExpression();
        String formattedExpression = populatedConvertible.toFormattedJsonExpression();
        String toString = populatedConvertible.toString();

        // All methods should produce equivalent JSON data
        JsonObject fromExpression = new JsonObject(jsonExpression);
        JsonObject fromFormatted = new JsonObject(formattedExpression);
        JsonObject fromToString = new JsonObject(toString);

        assertEquals(jsonObject.encode(), fromExpression.encode());
        assertEquals(jsonObject.encode(), fromFormatted.encode());
        assertEquals(jsonObject.encode(), fromToString.encode());

        // toString should equal toJsonExpression
        assertEquals(jsonExpression, toString);
    }


    @Test
    void testNullHandling() {
        // Test handling of null values in JSON
        JsonObject nullData = new JsonObject()
                .put("nullString", null)
                .put("nullNumber", null)
                .put("nullBoolean", null);

        TestJsonObjectConvertible nullConvertible = new TestJsonObjectConvertible(nullData);
        JsonObject result = nullConvertible.toJsonObject();

        assertNull(result.getValue("nullString"));
        assertNull(result.getValue("nullNumber"));
        assertNull(result.getValue("nullBoolean"));
    }

    @Test
    void testSpecialCharacters() {
        // Test handling of special characters in JSON
        JsonObject specialData = new JsonObject()
                .put("quotes", "He said \"Hello\"")
                .put("newline", "Line 1\nLine 2")
                .put("tab", "Column1\tColumn2")
                .put("unicode", "中文测试");

        TestJsonObjectConvertible specialConvertible = new TestJsonObjectConvertible(specialData);
        JsonObject result = specialConvertible.toJsonObject();

        assertEquals("He said \"Hello\"", result.getString("quotes"));
        assertEquals("Line 1\nLine 2", result.getString("newline"));
        assertEquals("Column1\tColumn2", result.getString("tab"));
        assertEquals("中文测试", result.getString("unicode"));
    }

    /**
     * A concrete implementation of JsonObjectConvertible for testing purposes.
     */
    private static class TestJsonObjectConvertible implements JsonObjectConvertible {
        private final JsonObject jsonObject;

        public TestJsonObjectConvertible(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        public JsonObject toJsonObject() {
            return jsonObject;
        }

        @Override
        public String toJsonExpression() {
            return jsonObject.encode();
        }

        @Override
        public String toFormattedJsonExpression() {
            return jsonObject.encodePrettily();
        }

        @Override
        public String toString() {
            return toJsonExpression();
        }
    }
}