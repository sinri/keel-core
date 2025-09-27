package io.github.sinri.keel.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class JsonifiableSerializerTest extends KeelJUnit5Test {

    private ObjectMapper objectMapper;

    public JsonifiableSerializerTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeAll
    static void prepare() {
        JsonifiableSerializer.register();
    }

    @BeforeEach
    void setUp() {
        objectMapper = DatabindCodec.mapper();
    }

    @Test
    @SuppressWarnings("deprecation")
    void testBasicSerializationWithJsonifiableEntityImpl() throws JsonProcessingException {
        // Test with JsonifiableEntityImpl (deprecated but still supported)
        TestJsonifiableEntity entity = new TestJsonifiableEntity();
        entity.ensureEntry("string", "test value");
        entity.ensureEntry("number", 42);
        entity.ensureEntry("boolean", true);
        entity.ensureEntry("null_value", null);

        String json = objectMapper.writeValueAsString(entity);
        getUnitTestLogger().info("Serialized JSON: " + json);

        // Verify the JSON contains expected values
        assertTrue(json.contains("\"string\":\"test value\""));
        assertTrue(json.contains("\"number\":42"));
        assertTrue(json.contains("\"boolean\":true"));
        assertTrue(json.contains("\"null_value\":null"));
    }

    @Test
    void testBasicSerializationWithJsonifiableDataUnitImpl() throws JsonProcessingException {
        // Test with JsonifiableDataUnitImpl (modern implementation)
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        dataUnit.toJsonObject().put("name", "test data unit");
        dataUnit.toJsonObject().put("count", 100);
        dataUnit.toJsonObject().put("active", false);

        String json = objectMapper.writeValueAsString(dataUnit);
        getUnitTestLogger().info("Serialized JSON: " + json);

        // Verify the JSON contains expected values
        assertTrue(json.contains("\"name\":\"test data unit\""));
        assertTrue(json.contains("\"count\":100"));
        assertTrue(json.contains("\"active\":false"));
    }

    @Test
    void testBasicSerializationWithUnmodifiableJsonifiableEntity() throws JsonProcessingException {
        // Test with UnmodifiableJsonifiableEntity
        JsonObject jsonObject = new JsonObject()
                .put("immutable", "value")
                .put("readonly", 999);
        
        UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(jsonObject);

        String json = objectMapper.writeValueAsString(entity);
        getUnitTestLogger().info("Serialized JSON: " + json);

        // Verify the JSON contains expected values
        assertTrue(json.contains("\"immutable\":\"value\""));
        assertTrue(json.contains("\"readonly\":999"));
    }

    @Test
    void testNestedObjectSerialization() throws JsonProcessingException {
        TestJsonifiableDataUnit parent = new TestJsonifiableDataUnit();
        parent.toJsonObject().put("parent_field", "parent_value");

        TestJsonifiableDataUnit child = new TestJsonifiableDataUnit();
        child.toJsonObject().put("child_field", "child_value");
        child.toJsonObject().put("child_number", 123);

        parent.toJsonObject().put("child", child);

        String json = objectMapper.writeValueAsString(parent);
        getUnitTestLogger().info("Serialized JSON with nested object: " + json);

        // Verify nested structure
        assertTrue(json.contains("\"parent_field\":\"parent_value\""));
        assertTrue(json.contains("\"child\":{"));
        assertTrue(json.contains("\"child_field\":\"child_value\""));
        assertTrue(json.contains("\"child_number\":123"));
    }

    @Test
    void testArraySerialization() throws JsonProcessingException {
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        dataUnit.toJsonObject().put("items", List.of("item1", "item2", "item3"));
        dataUnit.toJsonObject().put("numbers", List.of(1, 2, 3, 4, 5));
        dataUnit.toJsonObject().put("mixed", Arrays.asList("string", 42, true, null));

        String json = objectMapper.writeValueAsString(dataUnit);
        getUnitTestLogger().info("Serialized JSON with arrays: " + json);

        // Verify array content
        assertTrue(json.contains("\"items\":[\"item1\",\"item2\",\"item3\"]"));
        assertTrue(json.contains("\"numbers\":[1,2,3,4,5]"));
        assertTrue(json.contains("\"mixed\":[\"string\",42,true,null]"));
    }

    @Test
    void testEmptyObjectSerialization() throws JsonProcessingException {
        TestJsonifiableDataUnit emptyDataUnit = new TestJsonifiableDataUnit();

        String json = objectMapper.writeValueAsString(emptyDataUnit);
        getUnitTestLogger().info("Serialized empty JSON: " + json);

        // Should serialize to empty object or contain only expected fields
        assertTrue(json.equals("{}") || json.contains("empty"));
    }

    @Test
    void testComplexNestedStructure() throws JsonProcessingException {
        TestJsonifiableDataUnit root = new TestJsonifiableDataUnit();
        root.toJsonObject().put("level", 1);

        TestJsonifiableDataUnit level2 = new TestJsonifiableDataUnit();
        level2.toJsonObject().put("level", 2);
        level2.toJsonObject().put("items", List.of("a", "b", "c"));

        TestJsonifiableDataUnit level3 = new TestJsonifiableDataUnit();
        level3.toJsonObject().put("level", 3);
        level3.toJsonObject().put("final", true);

        level2.toJsonObject().put("nested", level3);
        root.toJsonObject().put("nested", level2);

        String json = objectMapper.writeValueAsString(root);
        getUnitTestLogger().info("Serialized complex nested JSON: " + json);

        // Verify complex structure
        assertTrue(json.contains("\"level\":1"));
        assertTrue(json.contains("\"nested\":{"));
        assertTrue(json.contains("\"level\":2"));
        assertTrue(json.contains("\"items\":[\"a\",\"b\",\"c\"]"));
        assertTrue(json.contains("\"nested\":{"));
        assertTrue(json.contains("\"level\":3"));
        assertTrue(json.contains("\"final\":true"));
    }

    @Test
    void testJsonRoundTrip() throws JsonProcessingException {
        TestJsonifiableDataUnit original = new TestJsonifiableDataUnit();
        original.toJsonObject().put("test", "roundtrip");
        original.toJsonObject().put("number", 42);
        original.toJsonObject().put("array", List.of(1, 2, 3));

        // Serialize to JSON string
        String jsonString = objectMapper.writeValueAsString(original);
        getUnitTestLogger().info("Original JSON: " + jsonString);

        // Parse back to JsonObject
        JsonObject parsedJson = new JsonObject(jsonString);
        getUnitTestLogger().info("Parsed JsonObject: " + parsedJson);

        // Verify content matches
        assertEquals("roundtrip", parsedJson.getString("test"));
        assertEquals(42, parsedJson.getInteger("number"));
        assertEquals(List.of(1, 2, 3), parsedJson.getJsonArray("array").getList());
    }

    @Test
    void testSerializationInJsonObject() throws JsonProcessingException {
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        dataUnit.toJsonObject().put("embedded", "value");

        JsonObject wrapper = new JsonObject();
        wrapper.put("wrapper_field", "wrapper_value");
        wrapper.put("data_unit", dataUnit);

        String json = objectMapper.writeValueAsString(wrapper);
        getUnitTestLogger().info("Serialized JSON with embedded JsonSerializable: " + json);

        // Verify the embedded object is properly serialized
        assertTrue(json.contains("\"wrapper_field\":\"wrapper_value\""));
        assertTrue(json.contains("\"data_unit\":{"));
        assertTrue(json.contains("\"embedded\":\"value\""));
    }

    @Test
    void testMultipleJsonSerializableObjects() throws JsonProcessingException {
        TestJsonifiableDataUnit unit1 = new TestJsonifiableDataUnit();
        unit1.toJsonObject().put("id", 1);
        unit1.toJsonObject().put("name", "first");

        TestJsonifiableDataUnit unit2 = new TestJsonifiableDataUnit();
        unit2.toJsonObject().put("id", 2);
        unit2.toJsonObject().put("name", "second");

        JsonObject container = new JsonObject();
        container.put("units", List.of(unit1, unit2));

        String json = objectMapper.writeValueAsString(container);
        getUnitTestLogger().info("Serialized JSON with multiple JsonSerializable objects: " + json);

        // Verify both objects are properly serialized
        assertTrue(json.contains("\"units\":["));
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"first\""));
        assertTrue(json.contains("\"id\":2"));
        assertTrue(json.contains("\"name\":\"second\""));
    }

    @Test
    void testSerializerRegistration() {
        // Test that registration doesn't throw exceptions
        assertDoesNotThrow(() -> {
            JsonifiableSerializer.register();
        });

        // Test that multiple registrations don't cause issues
        assertDoesNotThrow(() -> {
            JsonifiableSerializer.register();
            JsonifiableSerializer.register();
        });
    }

    @Test
    void testSpecialCharactersInValues() throws JsonProcessingException {
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        dataUnit.toJsonObject().put("special_chars", "test\"quotes\"and\\backslashes");
        dataUnit.toJsonObject().put("unicode", "测试中文和emoji 🚀");
        dataUnit.toJsonObject().put("newlines", "line1\nline2\rline3");

        String json = objectMapper.writeValueAsString(dataUnit);
        getUnitTestLogger().info("Serialized JSON with special characters: " + json);

        // Verify special characters are properly escaped
        assertTrue(json.contains("test\\\"quotes\\\"and\\\\backslashes"));
        assertTrue(json.contains("测试中文和emoji 🚀"));
        assertTrue(json.contains("line1\\nline2\\rline3"));
    }

    @Test
    void testErrorHandlingWithInvalidJsonSerializable() {
        // Test with a JsonSerializable that returns invalid JSON
        InvalidJsonSerializable invalid = new InvalidJsonSerializable();
        
        // This should throw an exception when trying to serialize
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.writeValueAsString(invalid);
        });
    }

    @Test
    void testNullValueHandling() throws JsonProcessingException {
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        dataUnit.toJsonObject().put("null_field", null);
        dataUnit.toJsonObject().put("empty_string", "");
        dataUnit.toJsonObject().put("zero_number", 0);

        String json = objectMapper.writeValueAsString(dataUnit);
        getUnitTestLogger().info("Serialized JSON with null values: " + json);

        // Verify null values are properly handled
        assertTrue(json.contains("\"null_field\":null"));
        assertTrue(json.contains("\"empty_string\":\"\""));
        assertTrue(json.contains("\"zero_number\":0"));
    }

    @Test
    void testLargeDataSerialization() throws JsonProcessingException {
        TestJsonifiableDataUnit dataUnit = new TestJsonifiableDataUnit();
        
        // Add a large number of entries
        for (int i = 0; i < 1000; i++) {
            dataUnit.toJsonObject().put("key_" + i, "value_" + i);
        }

        String json = objectMapper.writeValueAsString(dataUnit);
        getUnitTestLogger().info("Serialized large JSON (length: " + json.length() + ")");

        // Verify the JSON contains expected entries
        assertTrue(json.contains("\"key_0\":\"value_0\""));
        assertTrue(json.contains("\"key_999\":\"value_999\""));
        assertTrue(json.length() > 10000); // Should be a substantial JSON
    }

    // Test implementation classes
    @SuppressWarnings("deprecation")
    private static class TestJsonifiableEntity extends JsonifiableEntityImpl<TestJsonifiableEntity> {
        public TestJsonifiableEntity() {
            super();
        }

        @Nonnull
        @Override
        public TestJsonifiableEntity getImplementation() {
            return this;
        }
    }

    private static class TestJsonifiableDataUnit extends JsonifiableDataUnitImpl {
        public TestJsonifiableDataUnit() {
            super();
        }
    }

    private static class InvalidJsonSerializable implements JsonSerializable {
        @Override
        public String toJsonExpression() {
            return "invalid json {"; // Invalid JSON that will cause parsing error
        }

        @Override
        public String toFormattedJsonExpression() {
            return toJsonExpression();
        }

        @Override
        public String toString() {
            return toJsonExpression();
        }
    }
}