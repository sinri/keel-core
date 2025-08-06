package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonifiableDataUnitTest extends KeelUnitTest {

    private JsonifiableDataUnit dataUnit;
    private JsonObject testData;

    @BeforeEach
    public void setUp() {
        testData = new JsonObject()
                .put("string", "test string")
                .put("number", 42)
                .put("long", 123456789L)
                .put("integer", 100)
                .put("float", 3.14f)
                .put("double", 2.718)
                .put("boolean", true)
                .put("null", null)
                .put("object", new JsonObject().put("nested", "value"))
                .put("array", new JsonArray().add("item1").add("item2").add(3))
                .put("stringArray", new JsonArray().add("a").add("b").add("c"))
                .put("numberArray", new JsonArray().add(1).add(2).add(3))
                .put("nested", new JsonObject()
                        .put("level1", new JsonObject()
                                .put("level2", "deep value")));

        dataUnit = new JsonifiableDataUnitImpl(testData);
    }

    @Test
    void testConstructorWithJsonObject() {
        JsonifiableDataUnit unit = new JsonifiableDataUnitImpl(testData);
        assertNotNull(unit);
        assertEquals(testData, unit.toJsonObject());
    }

    @Test
    void testDefaultConstructor() {
        JsonifiableDataUnit unit = new JsonifiableDataUnitImpl();
        assertNotNull(unit);
        assertTrue(unit.toJsonObject().isEmpty());
    }

    @Test
    void testToJsonObject() {
        JsonObject result = dataUnit.toJsonObject();
        assertNotNull(result);
        assertEquals(testData, result);
        assertEquals("test string", result.getString("string"));
        assertEquals(42, result.getInteger("number"));
    }

    @Test
    void testReloadData() {
        JsonObject newData = new JsonObject().put("new", "data");
        dataUnit.reloadData(newData);
        assertEquals(newData, dataUnit.toJsonObject());
        assertEquals("data", dataUnit.toJsonObject().getString("new"));
    }

    @Test
    void testToJsonExpression() {
        String jsonExpression = dataUnit.toJsonExpression();
        assertNotNull(jsonExpression);
        assertTrue(jsonExpression.contains("\"string\":\"test string\""));
        assertTrue(jsonExpression.contains("\"number\":42"));
    }

    @Test
    void testToFormattedJsonExpression() {
        String formattedJson = dataUnit.toFormattedJsonExpression();
        assertNotNull(formattedJson);
        assertTrue(formattedJson.contains("\n"));
        assertTrue(formattedJson.contains("  ")); // indentation
        assertTrue(formattedJson.contains("\"string\""));
        assertTrue(formattedJson.contains("\"test string\""));
    }

    @Test
    void testReadWithJsonPointer() {
        // Test reading string value
        String result = dataUnit.read(jsonPointer -> {
            jsonPointer.append("string");
            return String.class;
        });
        assertEquals("test string", result);

        // Test reading nested value
        String nestedResult = dataUnit.read(jsonPointer -> {
            jsonPointer.append("nested").append("level1").append("level2");
            return String.class;
        });
        assertEquals("deep value", nestedResult);

        // Test reading non-existent value
        String nonExistent = dataUnit.read(jsonPointer -> {
            jsonPointer.append("nonExistent");
            return String.class;
        });
        assertNull(nonExistent);
    }

    @Test
    void testReadString() {
        assertEquals("test string", dataUnit.readString("string"));
        assertEquals("deep value", dataUnit.readString("nested", "level1", "level2"));
        assertNull(dataUnit.readString("nonExistent"));
        assertNull(dataUnit.readString("null"));
    }

    @Test
    void testReadStringRequired() {
        assertEquals("test string", dataUnit.readStringRequired("string"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readStringRequired("nonExistent");
        });

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readStringRequired("null");
        });
    }

    @Test
    void testReadNumber() {
        assertEquals(42, dataUnit.readNumber("number"));
        assertEquals(123456789L, dataUnit.readNumber("long"));
        assertEquals(100, dataUnit.readNumber("integer"));
        assertEquals(3.14f, dataUnit.readNumber("float"));
        assertEquals(2.718, dataUnit.readNumber("double"));
        assertNull(dataUnit.readNumber("nonExistent"));
    }

    @Test
    void testReadNumberRequired() {
        assertEquals(42, dataUnit.readNumberRequired("number"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readNumberRequired("nonExistent");
        });
    }

    @Test
    void testReadLong() {
        assertEquals(123456789L, dataUnit.readLong("long"));
        assertEquals(42L, dataUnit.readLong("number"));
        assertEquals(100L, dataUnit.readLong("integer"));
        assertNull(dataUnit.readLong("nonExistent"));
    }

    @Test
    void testReadLongRequired() {
        assertEquals(123456789L, dataUnit.readLongRequired("long"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readLongRequired("nonExistent");
        });
    }

    @Test
    void testReadInteger() {
        assertEquals(100, dataUnit.readInteger("integer"));
        assertEquals(42, dataUnit.readInteger("number"));
        assertEquals(123456789, dataUnit.readInteger("long"));
        assertNull(dataUnit.readInteger("nonExistent"));
    }

    @Test
    void testReadIntegerRequired() {
        assertEquals(100, dataUnit.readIntegerRequired("integer"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readIntegerRequired("nonExistent");
        });
    }

    @Test
    void testReadFloat() {
        assertEquals(3.14f, dataUnit.readFloat("float"));
        assertEquals(42.0f, dataUnit.readFloat("number"));
        assertNull(dataUnit.readFloat("nonExistent"));
    }

    @Test
    void testReadFloatRequired() {
        assertEquals(3.14f, dataUnit.readFloatRequired("float"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readFloatRequired("nonExistent");
        });
    }

    @Test
    void testReadDouble() {
        assertEquals(2.718, dataUnit.readDouble("double"));
        assertEquals(42.0, dataUnit.readDouble("number"));
        assertNull(dataUnit.readDouble("nonExistent"));
    }

    @Test
    void testReadDoubleRequired() {
        assertEquals(2.718, dataUnit.readDoubleRequired("double"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readDoubleRequired("nonExistent");
        });
    }

    @Test
    void testReadBoolean() {
        assertTrue(dataUnit.readBoolean("boolean"));
        assertNull(dataUnit.readBoolean("nonExistent"));
    }

    @Test
    void testReadBooleanRequired() {
        assertTrue(dataUnit.readBooleanRequired("boolean"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readBooleanRequired("nonExistent");
        });
    }

    @Test
    void testReadJsonObject() {
        JsonObject result = dataUnit.readJsonObject("object");
        assertNotNull(result);
        assertEquals("value", result.getString("nested"));

        JsonObject nestedResult = dataUnit.readJsonObject("nested", "level1");
        assertNotNull(nestedResult);
        assertEquals("deep value", nestedResult.getString("level2"));

        assertNull(dataUnit.readJsonObject("nonExistent"));
    }

    @Test
    void testReadJsonObjectRequired() {
        JsonObject result = dataUnit.readJsonObjectRequired("object");
        assertNotNull(result);
        assertEquals("value", result.getString("nested"));

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readJsonObjectRequired("nonExistent");
        });
    }

    @Test
    void testReadJsonArray() {
        JsonArray result = dataUnit.readJsonArray("array");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("item1", result.getString(0));
        assertEquals("item2", result.getString(1));
        assertEquals(3, result.getInteger(2));

        assertNull(dataUnit.readJsonArray("nonExistent"));
    }

    @Test
    void testReadJsonArrayRequired() {
        JsonArray result = dataUnit.readJsonArrayRequired("array");
        assertNotNull(result);
        assertEquals(3, result.size());

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readJsonArrayRequired("nonExistent");
        });
    }

    @Test
    void testReadStringArray() {
        List<String> result = dataUnit.readStringArray("stringArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));

        assertNull(dataUnit.readStringArray("nonExistent"));
    }

    @Test
    void testReadStringArrayRequired() {
        List<String> result = dataUnit.readStringArrayRequired("stringArray");
        assertNotNull(result);
        assertEquals(3, result.size());

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readStringArrayRequired("nonExistent");
        });
    }

    @Test
    void testReadIntegerArray() {
        List<Integer> result = dataUnit.readIntegerArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));

        assertNull(dataUnit.readIntegerArray("nonExistent"));
    }

    @Test
    void testReadIntegerArrayRequired() {
        List<Integer> result = dataUnit.readIntegerArrayRequired("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readIntegerArrayRequired("nonExistent");
        });
    }

    @Test
    void testReadLongArray() {
        List<Long> result = dataUnit.readLongArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));
        assertEquals(3L, result.get(2));
    }

    @Test
    void testReadFloatArray() {
        List<Float> result = dataUnit.readFloatArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0f, result.get(0));
        assertEquals(2.0f, result.get(1));
        assertEquals(3.0f, result.get(2));
    }

    @Test
    void testReadDoubleArray() {
        List<Double> result = dataUnit.readDoubleArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0, result.get(0));
        assertEquals(2.0, result.get(1));
        assertEquals(3.0, result.get(2));
    }

    @Test
    void testReadValue() {
        Object stringValue = dataUnit.readValue("string");
        assertEquals("test string", stringValue);

        Object numberValue = dataUnit.readValue("number");
        assertEquals(42, numberValue);

        Object booleanValue = dataUnit.readValue("boolean");
        assertEquals(true, booleanValue);

        assertNull(dataUnit.readValue("nonExistent"));
    }

    @Test
    void testReadValueRequired() {
        Object stringValue = dataUnit.readValueRequired("string");
        assertEquals("test string", stringValue);

        assertThrows(NullPointerException.class, () -> {
            dataUnit.readValueRequired("nonExistent");
        });
    }

    @Test
    void testEnsureEntry() {
        dataUnit.ensureEntry("newKey", "newValue");
        assertEquals("newValue", dataUnit.toJsonObject().getString("newKey"));

        // Test overwriting existing entry
        dataUnit.ensureEntry("string", "overwritten");
        assertEquals("overwritten", dataUnit.toJsonObject().getString("string"));
    }

    @Test
    void testRemoveEntry() {
        assertTrue(dataUnit.toJsonObject().containsKey("string"));
        dataUnit.removeEntry("string");
        assertFalse(dataUnit.toJsonObject().containsKey("string"));
        assertNull(dataUnit.toJsonObject().getString("string"));
    }

    @Test
    void testEnsureJsonObject() {
        JsonObject result = dataUnit.ensureJsonObject("newObject");
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(dataUnit.toJsonObject().containsKey("newObject"));

        // Test with existing JsonObject
        JsonObject existing = dataUnit.ensureJsonObject("object");
        assertNotNull(existing);
        assertEquals("value", existing.getString("nested"));
    }

    @Test
    void testEnsureJsonArray() {
        JsonArray result = dataUnit.ensureJsonArray("newArray");
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(dataUnit.toJsonObject().containsKey("newArray"));

        // Test with existing JsonArray
        JsonArray existing = dataUnit.ensureJsonArray("array");
        assertNotNull(existing);
        assertEquals(3, existing.size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(dataUnit.isEmpty());

        JsonifiableDataUnit emptyUnit = new JsonifiableDataUnitImpl();
        assertTrue(emptyUnit.isEmpty());
    }

    @Test
    void testIterator() {
        Iterator<Map.Entry<String, Object>> iterator = dataUnit.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());

        boolean foundString = false;
        boolean foundNumber = false;
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if ("string".equals(entry.getKey())) {
                assertEquals("test string", entry.getValue());
                foundString = true;
            } else if ("number".equals(entry.getKey())) {
                assertEquals(42, entry.getValue());
                foundNumber = true;
            }
        }
        assertTrue(foundString);
        assertTrue(foundNumber);
    }

    @Test
    void testWriteToBuffer() {
        Buffer buffer = Buffer.buffer();
        dataUnit.writeToBuffer(buffer);
        assertTrue(buffer.length() > 0);
    }

    @Test
    void testReadFromBuffer() {
        Buffer buffer = Buffer.buffer();
        dataUnit.writeToBuffer(buffer);

        JsonifiableDataUnit newUnit = new JsonifiableDataUnitImpl();
        int pos = newUnit.readFromBuffer(0, buffer);

        assertEquals(dataUnit.toJsonObject().encode(), newUnit.toJsonObject().encode());
        assertTrue(pos > 0);
    }

    @Test
    void testCloneAsJsonObject() {
        JsonObject cloned = dataUnit.cloneAsJsonObject();
        assertNotNull(cloned);
        assertEquals(dataUnit.toJsonObject().encode(), cloned.encode());

        // Verify it's a different instance
        assertNotSame(dataUnit.toJsonObject(), cloned);
    }

    @Test
    void testToBuffer() {
        Buffer buffer = dataUnit.toBuffer();
        assertNotNull(buffer);
        assertTrue(buffer.length() > 0);
    }

    @Test
    void testReadWithClassCastException() {
        // Test reading string as number should return null due to ClassCastException
        Number result = dataUnit.read(jsonPointer -> {
            jsonPointer.append("string");
            return Number.class;
        });
        assertNull(result);
    }

    @Test
    void testComplexNestedOperations() {
        // Test complex nested operations
        JsonifiableDataUnit complexUnit = new JsonifiableDataUnitImpl();

        // Create nested structure
        complexUnit.ensureEntry("level1", new JsonObject());
        complexUnit.ensureJsonObject("level1").put("level2", new JsonObject());
        complexUnit.ensureJsonObject("level1").getJsonObject("level2").put("level3", "deep value");

        // Read nested value
        String deepValue = complexUnit.readString("level1", "level2", "level3");
        assertEquals("deep value", deepValue);

        // Test array operations
        complexUnit.ensureEntry("items", new JsonArray());
        complexUnit.ensureJsonArray("items").add("item1").add("item2");

        List<String> items = complexUnit.readStringArray("items");
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("item1", items.get(0));
        assertEquals("item2", items.get(1));
    }

    @Test
    void testNullHandling() {
        // Test that null values are handled correctly
        assertNull(dataUnit.readString("null"));
        assertNull(dataUnit.readNumber("null"));
        assertNull(dataUnit.readBoolean("null"));
        assertNull(dataUnit.readJsonObject("null"));
        assertNull(dataUnit.readJsonArray("null"));
    }

    @Test
    void testToString() {
        String result = dataUnit.toString();
        assertNotNull(result);
        // toString() is not overridden in JsonifiableDataUnitImpl, so it uses Object.toString()
        // which returns the class name and hash code, not the JSON expression
        assertTrue(result.contains("JsonifiableDataUnitImpl"));
    }
}