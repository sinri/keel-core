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

@ExtendWith(VertxExtension.class)
class JsonSerializableTest extends KeelJUnit5Test {

    private TestJsonSerializable simpleObject;
    private TestJsonSerializable complexObject;
    private TestJsonSerializable emptyObject;
    private TestJsonSerializable nullObject;

    public JsonSerializableTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    public void setUp() {
        // Simple object with basic types
        simpleObject = new TestJsonSerializable();
        simpleObject.put("name", "John Doe");
        simpleObject.put("age", 30);
        simpleObject.put("active", true);

        // Complex object with nested structures
        complexObject = new TestJsonSerializable();
        complexObject.put("id", 12345);
        complexObject.put("user", new JsonObject()
                .put("name", "Jane Smith")
                .put("email", "jane@example.com")
                .put("roles", new JsonArray().add("admin").add("user")));
        complexObject.put("metadata", new JsonObject()
                .put("created", "2023-01-01")
                .put("tags", new JsonArray().add("important").add("urgent")));
        complexObject.put("scores", new JsonArray().add(95.5).add(87.2).add(92.8));

        // Empty object
        emptyObject = new TestJsonSerializable();

        // Object with null values
        nullObject = new TestJsonSerializable();
        nullObject.put("string", null);
        nullObject.put("number", null);
        nullObject.put("object", null);
        nullObject.put("array", null);
    }

    @Test
    void testToJsonExpression() {
        // Test simple object
        String simpleJson = simpleObject.toJsonExpression();
        assertNotNull(simpleJson);
        assertTrue(simpleJson.contains("\"name\":\"John Doe\""));
        assertTrue(simpleJson.contains("\"age\":30"));
        assertTrue(simpleJson.contains("\"active\":true"));
        assertFalse(simpleJson.contains("\n")); // Should be compact format

        // Test complex object
        String complexJson = complexObject.toJsonExpression();
        assertNotNull(complexJson);
        assertTrue(complexJson.contains("\"id\":12345"));
        assertTrue(complexJson.contains("\"name\":\"Jane Smith\""));
        assertTrue(complexJson.contains("\"email\":\"jane@example.com\""));
        assertTrue(complexJson.contains("\"admin\""));
        assertTrue(complexJson.contains("\"user\""));
        assertTrue(complexJson.contains("\"important\""));
        assertTrue(complexJson.contains("\"urgent\""));
        assertTrue(complexJson.contains("95.5"));
        assertTrue(complexJson.contains("87.2"));
        assertTrue(complexJson.contains("92.8"));

        // Test empty object
        String emptyJson = emptyObject.toJsonExpression();
        assertEquals("{}", emptyJson);

        // Test object with null values
        String nullJson = nullObject.toJsonExpression();
        assertNotNull(nullJson);
        assertTrue(nullJson.contains("\"string\":null"));
        assertTrue(nullJson.contains("\"number\":null"));
        assertTrue(nullJson.contains("\"object\":null"));
        assertTrue(nullJson.contains("\"array\":null"));
    }

    @Test
    void testToFormattedJsonExpression() {
        // Test simple object formatting
        String formattedSimple = simpleObject.toFormattedJsonExpression();
        assertNotNull(formattedSimple);
        assertTrue(formattedSimple.contains("\n")); // Should have line breaks
        assertTrue(formattedSimple.contains("  ")); // Should have indentation
        assertTrue(formattedSimple.contains("\"name\""));
        assertTrue(formattedSimple.contains("\"John Doe\""));
        assertTrue(formattedSimple.contains("\"age\""));
        assertTrue(formattedSimple.contains("30"));
        assertTrue(formattedSimple.contains("\"active\""));
        assertTrue(formattedSimple.contains("true"));

        // Test complex object formatting
        String formattedComplex = complexObject.toFormattedJsonExpression();
        assertNotNull(formattedComplex);
        assertTrue(formattedComplex.contains("\n"));
        assertTrue(formattedComplex.contains("  "));
        assertTrue(formattedComplex.contains("\"id\""));
        assertTrue(formattedComplex.contains("12345"));
        assertTrue(formattedComplex.contains("\"user\""));
        assertTrue(formattedComplex.contains("\"name\""));
        assertTrue(formattedComplex.contains("\"Jane Smith\""));
        assertTrue(formattedComplex.contains("\"roles\""));
        assertTrue(formattedComplex.contains("\"admin\""));
        assertTrue(formattedComplex.contains("\"user\""));
        assertTrue(formattedComplex.contains("\"metadata\""));
        assertTrue(formattedComplex.contains("\"created\""));
        assertTrue(formattedComplex.contains("\"2023-01-01\""));
        assertTrue(formattedComplex.contains("\"tags\""));
        assertTrue(formattedComplex.contains("\"important\""));
        assertTrue(formattedComplex.contains("\"urgent\""));
        assertTrue(formattedComplex.contains("\"scores\""));
        assertTrue(formattedComplex.contains("95.5"));
        assertTrue(formattedComplex.contains("87.2"));
        assertTrue(formattedComplex.contains("92.8"));

        // Test empty object formatting
        String formattedEmpty = emptyObject.toFormattedJsonExpression();
        assertEquals("{ }", formattedEmpty);

        // Test object with null values formatting
        String formattedNull = nullObject.toFormattedJsonExpression();
        assertNotNull(formattedNull);
        assertTrue(formattedNull.contains("\n"));
        // Check for indentation (spaces around colon in formatted JSON)
        assertTrue(formattedNull.contains("  "));
        assertTrue(formattedNull.contains("\"string\" : null"));
        assertTrue(formattedNull.contains("\"number\" : null"));
        assertTrue(formattedNull.contains("\"object\" : null"));
        assertTrue(formattedNull.contains("\"array\" : null"));
    }

    @Test
    void testToString() {
        // Test that toString() returns the same as toJsonExpression()
        String simpleToString = simpleObject.toString();
        String simpleJsonExpression = simpleObject.toJsonExpression();
        assertEquals(simpleJsonExpression, simpleToString);

        String complexToString = complexObject.toString();
        String complexJsonExpression = complexObject.toJsonExpression();
        assertEquals(complexJsonExpression, complexToString);

        String emptyToString = emptyObject.toString();
        String emptyJsonExpression = emptyObject.toJsonExpression();
        assertEquals(emptyJsonExpression, emptyToString);

        String nullToString = nullObject.toString();
        String nullJsonExpression = nullObject.toJsonExpression();
        assertEquals(nullJsonExpression, nullToString);
    }

    @Test
    void testJsonExpressionConsistency() {
        // Test that multiple calls return the same result
        String firstCall = simpleObject.toJsonExpression();
        String secondCall = simpleObject.toJsonExpression();
        String thirdCall = simpleObject.toJsonExpression();

        assertEquals(firstCall, secondCall);
        assertEquals(secondCall, thirdCall);
        assertEquals(firstCall, thirdCall);

        // Test complex object consistency
        String complexFirst = complexObject.toJsonExpression();
        String complexSecond = complexObject.toJsonExpression();
        assertEquals(complexFirst, complexSecond);
    }

    @Test
    void testFormattedJsonExpressionConsistency() {
        // Test that multiple calls return the same result
        String firstCall = simpleObject.toFormattedJsonExpression();
        String secondCall = simpleObject.toFormattedJsonExpression();
        String thirdCall = simpleObject.toFormattedJsonExpression();

        assertEquals(firstCall, secondCall);
        assertEquals(secondCall, thirdCall);
        assertEquals(firstCall, thirdCall);

        // Test complex object consistency
        String complexFirst = complexObject.toFormattedJsonExpression();
        String complexSecond = complexObject.toFormattedJsonExpression();
        assertEquals(complexFirst, complexSecond);
    }

    @Test
    void testJsonParsing() {
        // Test that the JSON expression can be parsed back to JsonObject
        String simpleJson = simpleObject.toJsonExpression();
        JsonObject parsedSimple = new JsonObject(simpleJson);
        assertEquals("John Doe", parsedSimple.getString("name"));
        assertEquals(30, parsedSimple.getInteger("age"));
        assertEquals(true, parsedSimple.getBoolean("active"));

        String complexJson = complexObject.toJsonExpression();
        JsonObject parsedComplex = new JsonObject(complexJson);
        assertEquals(12345, parsedComplex.getInteger("id"));

        JsonObject user = parsedComplex.getJsonObject("user");
        assertNotNull(user);
        assertEquals("Jane Smith", user.getString("name"));
        assertEquals("jane@example.com", user.getString("email"));

        JsonArray roles = user.getJsonArray("roles");
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertEquals("admin", roles.getString(0));
        assertEquals("user", roles.getString(1));

        String nullJson = nullObject.toJsonExpression();
        JsonObject parsedNull = new JsonObject(nullJson);
        assertNull(parsedNull.getString("string"));
        assertNull(parsedNull.getValue("number"));
        assertNull(parsedNull.getValue("object"));
        assertNull(parsedNull.getValue("array"));
    }

    @Test
    void testSpecialCharacters() {
        TestJsonSerializable specialObject = new TestJsonSerializable();
        specialObject.put("quotes", "He said \"Hello World!\"");
        specialObject.put("newline", "Line 1\nLine 2");
        specialObject.put("tab", "Column1\tColumn2");
        specialObject.put("backslash", "C:\\Users\\Documents");
        specialObject.put("unicode", "中文测试");
        specialObject.put("emoji", "🚀🎉💻");

        String json = specialObject.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("\\\"")); // Escaped quotes
        assertTrue(json.contains("\\n")); // Escaped newline
        assertTrue(json.contains("\\t")); // Escaped tab
        assertTrue(json.contains("\\\\")); // Escaped backslash
        assertTrue(json.contains("中文测试")); // Unicode characters
        assertTrue(json.contains("🚀🎉💻")); // Emoji characters

        // Test parsing back
        JsonObject parsed = new JsonObject(json);
        assertEquals("He said \"Hello World!\"", parsed.getString("quotes"));
        assertEquals("Line 1\nLine 2", parsed.getString("newline"));
        assertEquals("Column1\tColumn2", parsed.getString("tab"));
        assertEquals("C:\\Users\\Documents", parsed.getString("backslash"));
        assertEquals("中文测试", parsed.getString("unicode"));
        assertEquals("🚀🎉💻", parsed.getString("emoji"));
    }

    @Test
    void testNumericTypes() {
        TestJsonSerializable numericObject = new TestJsonSerializable();
        numericObject.put("int", 42);
        numericObject.put("long", 123456789L);
        numericObject.put("float", 3.14f);
        numericObject.put("double", 2.718281828459045);
        numericObject.put("zero", 0);
        numericObject.put("negative", -42);
        numericObject.put("maxInt", Integer.MAX_VALUE);
        numericObject.put("minInt", Integer.MIN_VALUE);
        numericObject.put("maxLong", Long.MAX_VALUE);
        numericObject.put("minLong", Long.MIN_VALUE);

        String json = numericObject.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("\"int\":42"));
        assertTrue(json.contains("\"long\":123456789"));
        assertTrue(json.contains("\"float\":3.14"));
        assertTrue(json.contains("\"double\":2.718281828459045"));
        assertTrue(json.contains("\"zero\":0"));
        assertTrue(json.contains("\"negative\":-42"));
        assertTrue(json.contains("\"maxInt\":" + Integer.MAX_VALUE));
        assertTrue(json.contains("\"minInt\":" + Integer.MIN_VALUE));
        assertTrue(json.contains("\"maxLong\":" + Long.MAX_VALUE));
        assertTrue(json.contains("\"minLong\":" + Long.MIN_VALUE));

        // Test parsing back
        JsonObject parsed = new JsonObject(json);
        assertEquals(42, parsed.getInteger("int"));
        assertEquals(123456789L, parsed.getLong("long"));
        assertEquals(3.14f, parsed.getFloat("float"), 0.001f);
        assertEquals(2.718281828459045, parsed.getDouble("double"), 0.000000000000001);
        assertEquals(0, parsed.getInteger("zero"));
        assertEquals(-42, parsed.getInteger("negative"));
        assertEquals(Integer.MAX_VALUE, parsed.getInteger("maxInt"));
        assertEquals(Integer.MIN_VALUE, parsed.getInteger("minInt"));
        assertEquals(Long.MAX_VALUE, parsed.getLong("maxLong"));
        assertEquals(Long.MIN_VALUE, parsed.getLong("minLong"));
    }

    @Test
    void testBooleanTypes() {
        TestJsonSerializable booleanObject = new TestJsonSerializable();
        booleanObject.put("true", true);
        booleanObject.put("false", false);

        String json = booleanObject.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("\"true\":true"));
        assertTrue(json.contains("\"false\":false"));

        // Test parsing back
        JsonObject parsed = new JsonObject(json);
        assertTrue(parsed.getBoolean("true"));
        assertFalse(parsed.getBoolean("false"));
    }

    @Test
    void testEmptyArraysAndObjects() {
        TestJsonSerializable emptyStructuresObject = new TestJsonSerializable();
        emptyStructuresObject.put("emptyArray", new JsonArray());
        emptyStructuresObject.put("emptyObject", new JsonObject());

        String json = emptyStructuresObject.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("\"emptyArray\":[]"));
        assertTrue(json.contains("\"emptyObject\":{}"));

        // Test parsing back
        JsonObject parsed = new JsonObject(json);
        assertNotNull(parsed.getJsonArray("emptyArray"));
        assertEquals(0, parsed.getJsonArray("emptyArray").size());
        assertNotNull(parsed.getJsonObject("emptyObject"));
        assertTrue(parsed.getJsonObject("emptyObject").isEmpty());
    }

    /**
     * Test implementation of JsonSerializable interface
     */
    private static class TestJsonSerializable implements JsonSerializable {
        private final JsonObject data;

        public TestJsonSerializable() {
            this.data = new JsonObject();
        }

        public void put(String key, Object value) {
            data.put(key, value);
        }

        @Override
        public String toJsonExpression() {
            return data.encode();
        }

        @Override
        public String toFormattedJsonExpression() {
            return data.encodePrettily();
        }

        @Override
        public String toString() {
            return toJsonExpression();
        }
    }
}