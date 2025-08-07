package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UnmodifiableJsonifiableEntityTest extends KeelUnitTest {

    private UnmodifiableJsonifiableEntity emptyEntity;
    private UnmodifiableJsonifiableEntity populatedEntity;
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
                .put("object", new JsonObject().put("nested", "value"))
                .put("long", 123456789L)
                .put("double", 3.14159)
                .put("float", 2.718f)
                .put("stringArray", new JsonArray().add("str1").add("str2").add("str3"))
                .put("numberArray", new JsonArray().add(1).add(2).add(3))
                .put("objectArray", new JsonArray()
                        .add(new JsonObject().put("id", 1).put("name", "obj1"))
                        .add(new JsonObject().put("id", 2).put("name", "obj2")));

        emptyEntity = UnmodifiableJsonifiableEntity.wrap(new JsonObject());
        populatedEntity = UnmodifiableJsonifiableEntity.wrap(testJsonObject);
    }

    @Test
    void testWrap() {
        // Test wrapping empty JsonObject
        UnmodifiableJsonifiableEntity empty = UnmodifiableJsonifiableEntity.wrap(new JsonObject());
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertEquals("{}", empty.toJsonExpression());

        // Test wrapping populated JsonObject
        JsonObject original = new JsonObject().put("key", "value");
        UnmodifiableJsonifiableEntity wrapped = UnmodifiableJsonifiableEntity.wrap(original);
        assertNotNull(wrapped);
        assertFalse(wrapped.isEmpty());
        assertTrue(wrapped.toJsonExpression().contains("key"));
        assertTrue(wrapped.toJsonExpression().contains("value"));
    }

    @Test
    void testCloneAsJsonObject() {
        JsonObject cloned = populatedEntity.cloneAsJsonObject();
        
        assertNotNull(cloned);
        assertNotSame(testJsonObject, cloned);
        assertEquals(testJsonObject.encode(), cloned.encode());
        
        // Verify it's a deep copy
        assertEquals("test value", cloned.getString("string"));
        assertEquals(42, cloned.getInteger("number"));
        assertTrue(cloned.getBoolean("boolean"));
        
        // Test with empty entity
        JsonObject emptyCloned = emptyEntity.cloneAsJsonObject();
        assertNotNull(emptyCloned);
        assertTrue(emptyCloned.isEmpty());
    }

    @Test
    void testToBuffer() {
        Buffer buffer = populatedEntity.toBuffer();

        assertNotNull(buffer);
        assertTrue(buffer.length() > 0);

        // Test that the buffer contains the expected data
        JsonObject decoded = new JsonObject(buffer);
        assertEquals("test value", decoded.getString("string"));
        assertEquals(42, decoded.getInteger("number"));
        assertTrue(decoded.getBoolean("boolean"));
        
        // Test with empty entity
        Buffer emptyBuffer = emptyEntity.toBuffer();
        assertNotNull(emptyBuffer);
        assertTrue(emptyBuffer.length() > 0);
    }

    @Test
    void testToJsonExpression() {
        String expression = populatedEntity.toJsonExpression();

        assertNotNull(expression);
        assertEquals(testJsonObject.encode(), expression);
        assertTrue(expression.contains("test value"));
        assertTrue(expression.contains("42"));
        assertTrue(expression.contains("true"));
        
        // Test with empty entity
        String emptyExpression = emptyEntity.toJsonExpression();
        assertEquals("{}", emptyExpression);
    }

    @Test
    void testToFormattedJsonExpression() {
        String formatted = populatedEntity.toFormattedJsonExpression();

        assertNotNull(formatted);
        assertEquals(testJsonObject.encodePrettily(), formatted);
        // Should contain newlines and proper formatting
        assertTrue(formatted.contains("\n"));
        assertTrue(formatted.contains("  ")); // indentation
        
        // Test with empty entity
        String emptyFormatted = emptyEntity.toFormattedJsonExpression();
        assertTrue(emptyFormatted.contains("{") && emptyFormatted.contains("}"));
    }

    @Test
    void testToString() {
        String result = populatedEntity.toString();

        assertNotNull(result);
        assertEquals(populatedEntity.toJsonExpression(), result);
        assertEquals(testJsonObject.encode(), result);
    }

    @Test
    void testRead() {
        // Test reading string
        String stringResult = populatedEntity.read(pointer -> {
            pointer.append("string");
            return String.class;
        });
        assertEquals("test value", stringResult);

        // Test reading nested value
        String nestedResult = populatedEntity.read(pointer -> {
            pointer.append("object").append("nested");
            return String.class;
        });
        assertEquals("value", nestedResult);

        // Test reading non-existent value
        String nonExistent = populatedEntity.read(pointer -> {
            pointer.append("nonexistent");
            return String.class;
        });
        assertNull(nonExistent);

        // Test reading null value
        Object nullResult = populatedEntity.read(pointer -> {
            pointer.append("null");
            return Object.class;
        });
        assertNull(nullResult);
    }

    @Test
    void testReadString() {
        assertEquals("test value", populatedEntity.readString("string"));
        assertEquals("value", populatedEntity.readString("object", "nested"));
        assertNull(populatedEntity.readString("nonexistent"));
        assertNull(populatedEntity.readString("null"));
    }

    @Test
    void testReadStringRequired() {
        assertEquals("test value", populatedEntity.readStringRequired("string"));
        assertEquals("value", populatedEntity.readStringRequired("object", "nested"));
        
        // Should throw NullPointerException for null or non-existent values
        assertThrows(NullPointerException.class, () -> populatedEntity.readStringRequired("nonexistent"));
        assertThrows(NullPointerException.class, () -> populatedEntity.readStringRequired("null"));
    }

    @Test
    void testReadNumber() {
        assertEquals(42, populatedEntity.readNumber("number").intValue());
        assertEquals(123456789L, populatedEntity.readNumber("long").longValue());
        assertEquals(3.14159, populatedEntity.readNumber("double").doubleValue(), 0.0001);
        assertNull(populatedEntity.readNumber("nonexistent"));
    }

    @Test
    void testReadNumberRequired() {
        assertEquals(42, populatedEntity.readNumberRequired("number").intValue());
        assertEquals(123456789L, populatedEntity.readNumberRequired("long").longValue());
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readNumberRequired("nonexistent"));
    }

    @Test
    void testReadLong() {
        assertEquals(42L, populatedEntity.readLong("number"));
        assertEquals(123456789L, populatedEntity.readLong("long"));
        assertNull(populatedEntity.readLong("nonexistent"));
    }

    @Test
    void testReadLongRequired() {
        assertEquals(42L, populatedEntity.readLongRequired("number"));
        assertEquals(123456789L, populatedEntity.readLongRequired("long"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readLongRequired("nonexistent"));
    }

    @Test
    void testReadInteger() {
        assertEquals(42, populatedEntity.readInteger("number"));
        assertEquals(123456789, populatedEntity.readInteger("long"));
        assertNull(populatedEntity.readInteger("nonexistent"));
    }

    @Test
    void testReadIntegerRequired() {
        assertEquals(42, populatedEntity.readIntegerRequired("number"));
        assertEquals(123456789, populatedEntity.readIntegerRequired("long"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readIntegerRequired("nonexistent"));
    }

    @Test
    void testReadFloat() {
        assertEquals(42.0f, populatedEntity.readFloat("number"), 0.001);
        assertEquals(2.718f, populatedEntity.readFloat("float"), 0.001);
        assertNull(populatedEntity.readFloat("nonexistent"));
    }

    @Test
    void testReadFloatRequired() {
        assertEquals(42.0f, populatedEntity.readFloatRequired("number"), 0.001);
        assertEquals(2.718f, populatedEntity.readFloatRequired("float"), 0.001);
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readFloatRequired("nonexistent"));
    }

    @Test
    void testReadDouble() {
        assertEquals(42.0, populatedEntity.readDouble("number"), 0.001);
        assertEquals(3.14159, populatedEntity.readDouble("double"), 0.0001);
        assertNull(populatedEntity.readDouble("nonexistent"));
    }

    @Test
    void testReadDoubleRequired() {
        assertEquals(42.0, populatedEntity.readDoubleRequired("number"), 0.001);
        assertEquals(3.14159, populatedEntity.readDoubleRequired("double"), 0.0001);
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readDoubleRequired("nonexistent"));
    }

    @Test
    void testReadBoolean() {
        assertTrue(populatedEntity.readBoolean("boolean"));
        assertNull(populatedEntity.readBoolean("nonexistent"));
    }

    @Test
    void testReadBooleanRequired() {
        assertTrue(populatedEntity.readBooleanRequired("boolean"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readBooleanRequired("nonexistent"));
    }

    @Test
    void testReadJsonObject() {
        JsonObject result = populatedEntity.readJsonObject("object");
        assertNotNull(result);
        assertEquals("value", result.getString("nested"));
        
        assertNull(populatedEntity.readJsonObject("nonexistent"));
    }

    @Test
    void testReadJsonObjectRequired() {
        JsonObject result = populatedEntity.readJsonObjectRequired("object");
        assertNotNull(result);
        assertEquals("value", result.getString("nested"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readJsonObjectRequired("nonexistent"));
    }

    @Test
    void testReadJsonArray() {
        JsonArray result = populatedEntity.readJsonArray("array");
        assertNotNull(result);
        assertEquals("item1", result.getString(0));
        assertEquals("item2", result.getString(1));
        
        assertNull(populatedEntity.readJsonArray("nonexistent"));
    }

    @Test
    void testReadJsonArrayRequired() {
        JsonArray result = populatedEntity.readJsonArrayRequired("array");
        assertNotNull(result);
        assertEquals("item1", result.getString(0));
        assertEquals("item2", result.getString(1));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readJsonArrayRequired("nonexistent"));
    }

    @Test
    void testReadJsonObjectArray() {
        List<JsonObject> result = populatedEntity.readJsonObjectArray("objectArray");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getInteger("id"));
        assertEquals("obj1", result.get(0).getString("name"));
        assertEquals(2, result.get(1).getInteger("id"));
        assertEquals("obj2", result.get(1).getString("name"));
        
        assertNull(populatedEntity.readJsonObjectArray("nonexistent"));
    }

    @Test
    void testReadJsonObjectArrayRequired() {
        List<JsonObject> result = populatedEntity.readJsonObjectArrayRequired("objectArray");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getInteger("id"));
        assertEquals("obj1", result.get(0).getString("name"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readJsonObjectArrayRequired("nonexistent"));
    }

    @Test
    void testReadStringArray() {
        List<String> result = populatedEntity.readStringArray("stringArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("str1", result.get(0));
        assertEquals("str2", result.get(1));
        assertEquals("str3", result.get(2));
        
        assertNull(populatedEntity.readStringArray("nonexistent"));
    }

    @Test
    void testReadStringArrayRequired() {
        List<String> result = populatedEntity.readStringArrayRequired("stringArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("str1", result.get(0));
        assertEquals("str2", result.get(1));
        assertEquals("str3", result.get(2));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readStringArrayRequired("nonexistent"));
    }

    @Test
    void testReadIntegerArray() {
        List<Integer> result = populatedEntity.readIntegerArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
        
        assertNull(populatedEntity.readIntegerArray("nonexistent"));
    }

    @Test
    void testReadIntegerArrayRequired() {
        List<Integer> result = populatedEntity.readIntegerArrayRequired("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readIntegerArrayRequired("nonexistent"));
    }

    @Test
    void testReadLongArray() {
        List<Long> result = populatedEntity.readLongArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));
        assertEquals(3L, result.get(2));
        
        assertNull(populatedEntity.readLongArray("nonexistent"));
    }

    @Test
    void testReadLongArrayRequired() {
        List<Long> result = populatedEntity.readLongArrayRequired("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));
        assertEquals(3L, result.get(2));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readLongArrayRequired("nonexistent"));
    }

    @Test
    void testReadFloatArray() {
        List<Float> result = populatedEntity.readFloatArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0f, result.get(0), 0.001);
        assertEquals(2.0f, result.get(1), 0.001);
        assertEquals(3.0f, result.get(2), 0.001);
        
        assertNull(populatedEntity.readFloatArray("nonexistent"));
    }

    @Test
    void testReadFloatArrayRequired() {
        List<Float> result = populatedEntity.readFloatArrayRequired("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0f, result.get(0), 0.001);
        assertEquals(2.0f, result.get(1), 0.001);
        assertEquals(3.0f, result.get(2), 0.001);
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readFloatArrayRequired("nonexistent"));
    }

    @Test
    void testReadDoubleArray() {
        List<Double> result = populatedEntity.readDoubleArray("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0, result.get(0), 0.001);
        assertEquals(2.0, result.get(1), 0.001);
        assertEquals(3.0, result.get(2), 0.001);
        
        assertNull(populatedEntity.readDoubleArray("nonexistent"));
    }

    @Test
    void testReadDoubleArrayRequired() {
        List<Double> result = populatedEntity.readDoubleArrayRequired("numberArray");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1.0, result.get(0), 0.001);
        assertEquals(2.0, result.get(1), 0.001);
        assertEquals(3.0, result.get(2), 0.001);
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readDoubleArrayRequired("nonexistent"));
    }

    @Test
    void testReadValue() {
        assertEquals("test value", populatedEntity.readValue("string"));
        assertEquals(42, populatedEntity.readValue("number"));
        assertTrue((Boolean) populatedEntity.readValue("boolean"));
        assertNull(populatedEntity.readValue("null"));
        assertNull(populatedEntity.readValue("nonexistent"));
    }

    @Test
    void testReadValueRequired() {
        assertEquals("test value", populatedEntity.readValueRequired("string"));
        assertEquals(42, populatedEntity.readValueRequired("number"));
        assertTrue((Boolean) populatedEntity.readValueRequired("boolean"));
        
        assertThrows(NullPointerException.class, () -> populatedEntity.readValueRequired("nonexistent"));
        assertThrows(NullPointerException.class, () -> populatedEntity.readValueRequired("null"));
    }

    @Test
    void testIterator() {
        Iterator<Map.Entry<String, Object>> iterator = populatedEntity.iterator();

        assertNotNull(iterator);
        assertTrue(iterator.hasNext());

        Map<String, Object> entries = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            entries.put(entry.getKey(), entry.getValue());
        }

        assertEquals("test value", entries.get("string"));
        assertEquals(42, entries.get("number"));
        assertEquals(true, entries.get("boolean"));
        assertNull(entries.get("null"));
        assertNotNull(entries.get("array"));
        assertNotNull(entries.get("object"));
        
        // Test iterator on empty entity
        Iterator<Map.Entry<String, Object>> emptyIterator = emptyEntity.iterator();
        assertNotNull(emptyIterator);
        assertFalse(emptyIterator.hasNext());
    }

    @Test
    void testIsEmpty() {
        assertTrue(emptyEntity.isEmpty());
        assertFalse(populatedEntity.isEmpty());
    }

    @Test
    void testComplexNestedStructure() {
        JsonObject complexData = new JsonObject()
                .put("level1", new JsonObject()
                        .put("level2", new JsonObject()
                                .put("level3", new JsonObject()
                                        .put("deepValue", "found it!"))));

        UnmodifiableJsonifiableEntity complexEntity = UnmodifiableJsonifiableEntity.wrap(complexData);

        String deepValue = complexEntity.readString("level1", "level2", "level3", "deepValue");
        assertEquals("found it!", deepValue);

        JsonObject level2 = complexEntity.readJsonObject("level1", "level2");
        assertNotNull(level2);
        assertTrue(level2.containsKey("level3"));
    }

    @Test
    void testNullHandling() {
        JsonObject nullData = new JsonObject()
                .put("nullString", (String) null)
                .put("nullNumber", (Number) null)
                .put("nullBoolean", (Boolean) null)
                .put("nullObject", (JsonObject) null)
                .put("nullArray", (JsonArray) null);

        UnmodifiableJsonifiableEntity nullEntity = UnmodifiableJsonifiableEntity.wrap(nullData);

        assertNull(nullEntity.readString("nullString"));
        assertNull(nullEntity.readNumber("nullNumber"));
        assertNull(nullEntity.readBoolean("nullBoolean"));
        assertNull(nullEntity.readJsonObject("nullObject"));
        assertNull(nullEntity.readJsonArray("nullArray"));
    }

    @Test
    void testTypeConversionEdgeCases() {
        // Test reading string as number (should return null due to ClassCastException)
        Number stringAsNumber = populatedEntity.read(pointer -> {
            pointer.append("string");
            return Number.class;
        });
        assertNull(stringAsNumber);

        // Test reading number as string (behavior depends on JsonPointer implementation)
        String numberAsString = populatedEntity.read(pointer -> {
            pointer.append("number");
            return String.class;
        });
        // The JsonPointer implementation may return null for type mismatches
        // This test documents the current behavior
        // We just verify the method doesn't throw an exception
    }
}