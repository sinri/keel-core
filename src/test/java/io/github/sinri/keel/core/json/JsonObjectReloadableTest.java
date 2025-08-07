package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonObjectReloadableTest extends KeelUnitTest {

    private TestJsonObjectReloadable testEntity;
    private JsonObject testJsonObject;

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

        testEntity = new TestJsonObjectReloadable();
    }

    @Test
    void testReloadDataWithValidJsonObject() {
        // Test reloading with a valid JsonObject
        testEntity.reloadData(testJsonObject);

        JsonObject result = testEntity.getData();
        assertNotNull(result);
        assertEquals("test value", result.getString("string"));
        assertEquals(42, result.getInteger("number"));
        assertTrue(result.getBoolean("boolean"));
        assertNull(result.getValue("null"));
        assertEquals("item1", result.getJsonArray("array").getString(0));
        assertEquals("item2", result.getJsonArray("array").getString(1));
        assertEquals("value", result.getJsonObject("object").getString("nested"));
    }

    @Test
    void testReloadDataWithEmptyJsonObject() {
        JsonObject emptyJson = new JsonObject();

        testEntity.reloadData(emptyJson);

        JsonObject result = testEntity.getData();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testReloadDataWithNullJsonObject() {
        // Test that the implementation handles null gracefully
        testEntity.reloadData(null);

        JsonObject result = testEntity.getData();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testReloadDataWithNullHandlingImplementation() {
        NullHandlingTestEntity entity = new NullHandlingTestEntity();

        // Test with valid data first
        entity.reloadData(testJsonObject);
        assertEquals("test value", entity.getData().getString("string"));

        // Test that null throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            entity.reloadData(null);
        });
    }

    @Test
    void testReloadDataOverwritesExistingData() {
        // First load some data
        JsonObject initialData = new JsonObject()
                .put("oldKey", "oldValue")
                .put("number", 100);

        testEntity.reloadData(initialData);
        assertEquals("oldValue", testEntity.getString("oldKey"));
        assertEquals(100, testEntity.getInteger("number"));

        // Then reload with new data
        JsonObject newData = new JsonObject()
                .put("newKey", "newValue")
                .put("number", 200);

        testEntity.reloadData(newData);

        // Old data should be gone
        assertNull(testEntity.getString("oldKey"));
        // New data should be present
        assertEquals("newValue", testEntity.getString("newKey"));
        assertEquals(200, testEntity.getInteger("number"));
    }

    @Test
    void testReloadDataWithComplexNestedStructure() {
        JsonObject complexData = new JsonObject()
                .put("level1", new JsonObject()
                        .put("level2", new JsonObject()
                                .put("level3", "deep value")
                                .put("array", new JsonArray().add("a").add("b").add("c")))
                        .put("simple", "simple value"))
                .put("rootArray", new JsonArray().add(1).add(2).add(3).add(4).add(5));

        testEntity.reloadData(complexData);

        JsonObject result = testEntity.getData();
        assertEquals("deep value", result.getJsonObject("level1").getJsonObject("level2").getString("level3"));
        assertEquals("simple value", result.getJsonObject("level1").getString("simple"));
        assertEquals("a", result.getJsonObject("level1").getJsonObject("level2").getJsonArray("array").getString(0));
        assertEquals("b", result.getJsonObject("level1").getJsonObject("level2").getJsonArray("array").getString(1));
        assertEquals("c", result.getJsonObject("level1").getJsonObject("level2").getJsonArray("array").getString(2));
        assertEquals(1, result.getJsonArray("rootArray").getInteger(0));
        assertEquals(5, result.getJsonArray("rootArray").getInteger(4));
    }

    @Test
    void testReloadDataWithDifferentDataTypes() {
        JsonObject mixedData = new JsonObject()
                .put("string", "text")
                .put("integer", 123)
                .put("long", 123456789L)
                .put("double", 3.14159)
                .put("boolean", true)
                .put("null", null)
                .put("array", new JsonArray().add(1).add("two").add(3.0).add(true).add(null))
                .put("object", new JsonObject().put("nested", "value"));

        testEntity.reloadData(mixedData);

        JsonObject result = testEntity.getData();
        assertEquals("text", result.getString("string"));
        assertEquals(123, result.getInteger("integer"));
        assertEquals(123456789L, result.getLong("long"));
        assertEquals(3.14159, result.getDouble("double"), 0.0001);
        assertTrue(result.getBoolean("boolean"));
        assertNull(result.getValue("null"));
        assertEquals(5, result.getJsonArray("array").size());
        assertEquals(1, result.getJsonArray("array").getInteger(0));
        assertEquals("two", result.getJsonArray("array").getString(1));
        assertEquals(3.0, result.getJsonArray("array").getDouble(2));
        assertTrue(result.getJsonArray("array").getBoolean(3));
        assertNull(result.getJsonArray("array").getValue(4));
        assertEquals("value", result.getJsonObject("object").getString("nested"));
    }

    @Test
    void testReloadDataPreservesJsonObjectImmutability() {
        JsonObject originalData = new JsonObject()
                .put("key", "original value");

        testEntity.reloadData(originalData);

        // Modify the original JsonObject
        originalData.put("key", "modified value");

        // The entity's data should not be affected
        assertEquals("original value", testEntity.getString("key"));
    }

    @Test
    void testReloadDataWithLargeJsonObject() {
        JsonObject largeData = new JsonObject();

        // Create a large JsonObject with many fields
        for (int i = 0; i < 1000; i++) {
            largeData.put("key" + i, "value" + i);
        }

        testEntity.reloadData(largeData);

        JsonObject result = testEntity.getData();
        assertEquals(1000, result.size());
        assertEquals("value0", result.getString("key0"));
        assertEquals("value999", result.getString("key999"));
    }

    @Test
    void testReloadDataWithSpecialCharacters() {
        JsonObject specialData = new JsonObject()
                .put("unicode", "测试文本")
                .put("special", "!@#$%^&*()_+-=[]{}|;':\",./<>?")
                .put("newlines", "line1\nline2\r\nline3")
                .put("tabs", "tab1\ttab2\t\ttab3");

        testEntity.reloadData(specialData);

        JsonObject result = testEntity.getData();
        assertEquals("测试文本", result.getString("unicode"));
        assertEquals("!@#$%^&*()_+-=[]{}|;':\",./<>?", result.getString("special"));
        assertEquals("line1\nline2\r\nline3", result.getString("newlines"));
        assertEquals("tab1\ttab2\t\ttab3", result.getString("tabs"));
    }

    @Test
    void testReloadDataMultipleTimes() {
        // Test reloading data multiple times
        JsonObject data1 = new JsonObject().put("version", 1).put("data", "first");
        JsonObject data2 = new JsonObject().put("version", 2).put("data", "second");
        JsonObject data3 = new JsonObject().put("version", 3).put("data", "third");

        testEntity.reloadData(data1);
        assertEquals(1, testEntity.getInteger("version"));
        assertEquals("first", testEntity.getString("data"));

        testEntity.reloadData(data2);
        assertEquals(2, testEntity.getInteger("version"));
        assertEquals("second", testEntity.getString("data"));

        testEntity.reloadData(data3);
        assertEquals(3, testEntity.getInteger("version"));
        assertEquals("third", testEntity.getString("data"));
    }

    @Test
    void testReloadDataWithEmptyStringValues() {
        JsonObject emptyStringData = new JsonObject()
                .put("empty", "")
                .put("whitespace", "   ")
                .put("normal", "normal value");

        testEntity.reloadData(emptyStringData);

        JsonObject result = testEntity.getData();
        assertEquals("", result.getString("empty"));
        assertEquals("   ", result.getString("whitespace"));
        assertEquals("normal value", result.getString("normal"));
    }

    @Test
    void testReloadDataWithJsonObjectContainingNullValues() {
        JsonObject nullData = new JsonObject()
                .put("nullString", null)
                .put("nullInteger", null)
                .put("nullBoolean", null)
                .put("nullArray", null)
                .put("nullObject", null)
                .put("validValue", "valid");

        testEntity.reloadData(nullData);

        JsonObject result = testEntity.getData();
        assertNull(result.getString("nullString"));
        assertNull(result.getInteger("nullInteger"));
        assertNull(result.getBoolean("nullBoolean"));
        assertNull(result.getJsonArray("nullArray"));
        assertNull(result.getJsonObject("nullObject"));
        assertEquals("valid", result.getString("validValue"));
    }

    /**
     * Concrete implementation of JsonObjectReloadable for testing purposes
     */
    private static class TestJsonObjectReloadable implements JsonObjectReloadable {
        private JsonObject data = new JsonObject();

        @Override
        public void reloadData(JsonObject jsonObject) {
            this.data = jsonObject != null ? jsonObject.copy() : new JsonObject();
        }

        public JsonObject getData() {
            return data;
        }

        public String getString(String key) {
            return data.getString(key);
        }

        public Integer getInteger(String key) {
            return data.getInteger(key);
        }

        public Boolean getBoolean(String key) {
            return data.getBoolean(key);
        }
    }

    /**
     * Another implementation that handles null differently
     */
    private static class NullHandlingTestEntity implements JsonObjectReloadable {
        private JsonObject data = new JsonObject();

        @Override
        public void reloadData(JsonObject jsonObject) {
            if (jsonObject == null) {
                throw new IllegalArgumentException("JsonObject cannot be null");
            }
            this.data = jsonObject.copy();
        }

        public JsonObject getData() {
            return data;
        }
    }
}