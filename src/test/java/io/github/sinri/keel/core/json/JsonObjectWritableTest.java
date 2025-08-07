package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class JsonObjectWritableTest extends KeelUnitTest {

    private JsonObjectWritable writable;
    private JsonObject testData;

    @BeforeEach
    @Override
    public void setUp() {
        // 使用 JsonifiableDataUnitImpl 作为 JsonObjectWritable 的实现
        testData = new JsonObject()
                .put("string", "test string")
                .put("number", 42)
                .put("boolean", true)
                .put("object", new JsonObject().put("nested", "value"))
                .put("array", new JsonArray().add("item1").add("item2"));

        writable = new JsonifiableDataUnitImpl(testData);
    }

    @Test
    @DisplayName("测试 ensureEntry 方法 - 添加新键值对")
    void testEnsureEntryAddNew() {
        // 添加新的键值对
        writable.ensureEntry("newKey", "newValue");
        
        // 验证新键值对已添加
        assertEquals("newValue", writable.readString("newKey"));
        getUnitTestLogger().info("成功添加新键值对: newKey = newValue");
    }

    @Test
    @DisplayName("测试 ensureEntry 方法 - 替换现有键值对")
    void testEnsureEntryReplaceExisting() {
        // 验证原始值
        assertEquals("test string", writable.readString("string"));
        
        // 替换现有键值对
        writable.ensureEntry("string", "updated value");
        
        // 验证值已被替换
        assertEquals("updated value", writable.readString("string"));
        getUnitTestLogger().info("成功替换现有键值对: string = updated value");
    }

    @Test
    @DisplayName("测试 ensureEntry 方法 - 添加不同类型的值")
    void testEnsureEntryWithDifferentTypes() {
        // 测试添加不同类型的值
        writable.ensureEntry("integer", 123);
        writable.ensureEntry("long", 123456789L);
        writable.ensureEntry("float", 3.14f);
        writable.ensureEntry("double", 2.718);
        writable.ensureEntry("null", null);
        writable.ensureEntry("boolean", false);
        
        // 验证所有值都正确添加
        assertEquals(123, writable.readInteger("integer"));
        assertEquals(123456789L, writable.readLong("long"));
        assertEquals(3.14f, writable.readFloat("float"), 0.001f);
        assertEquals(2.718, writable.readDouble("double"), 0.001);
        assertNull(writable.readString("null"));
        assertEquals(false, writable.readBoolean("boolean"));
        
        getUnitTestLogger().info("成功添加不同类型的值");
    }

    @Test
    @DisplayName("测试 removeEntry 方法")
    void testRemoveEntry() {
        // 验证键存在
        assertTrue(writable.readString("string") != null);
        
        // 删除键值对
        writable.removeEntry("string");
        
        // 验证键已被删除
        assertNull(writable.readString("string"));
        getUnitTestLogger().info("成功删除键值对: string");
    }

    @Test
    @DisplayName("测试 removeEntry 方法 - 删除不存在的键")
    void testRemoveEntryNonExistent() {
        // 删除不存在的键应该不会抛出异常
        assertDoesNotThrow(() -> {
            writable.removeEntry("nonExistentKey");
        });
        
        getUnitTestLogger().info("删除不存在的键不会抛出异常");
    }

    @Test
    @DisplayName("测试 ensureJsonObject 方法 - 创建新的 JSON 对象")
    void testEnsureJsonObjectCreateNew() {
        // 确保键不存在
        assertNull(writable.readJsonObject("newObject"));
        
        // 创建新的 JSON 对象
        JsonObject newObject = writable.ensureJsonObject("newObject");
        
        // 验证返回的对象不为空
        assertNotNull(newObject);
        assertTrue(newObject.isEmpty());
        
        // 验证对象已添加到 writable 中
        JsonObject retrievedObject = writable.readJsonObject("newObject");
        assertNotNull(retrievedObject);
        assertTrue(retrievedObject.isEmpty());
        
        getUnitTestLogger().info("成功创建新的 JSON 对象");
    }

    @Test
    @DisplayName("测试 ensureJsonObject 方法 - 获取现有的 JSON 对象")
    void testEnsureJsonObjectGetExisting() {
        // 先添加一个 JSON 对象
        JsonObject originalObject = new JsonObject().put("key1", "value1").put("key2", "value2");
        writable.ensureEntry("existingObject", originalObject);
        
        // 获取现有的 JSON 对象
        JsonObject retrievedObject = writable.ensureJsonObject("existingObject");
        
        // 验证返回的是同一个对象
        assertNotNull(retrievedObject);
        assertEquals("value1", retrievedObject.getString("key1"));
        assertEquals("value2", retrievedObject.getString("key2"));
        
        getUnitTestLogger().info("成功获取现有的 JSON 对象");
    }

    @Test
    @DisplayName("测试 ensureJsonObject 方法 - 修改返回的对象")
    void testEnsureJsonObjectModifyReturned() {
        // 创建新的 JSON 对象
        JsonObject newObject = writable.ensureJsonObject("modifiableObject");
        
        // 修改返回的对象
        newObject.put("addedKey", "addedValue");
        
        // 验证修改已反映到 writable 中
        JsonObject retrievedObject = writable.readJsonObject("modifiableObject");
        assertEquals("addedValue", retrievedObject.getString("addedKey"));
        
        getUnitTestLogger().info("成功修改返回的 JSON 对象");
    }

    @Test
    @DisplayName("测试 ensureJsonArray 方法 - 创建新的 JSON 数组")
    void testEnsureJsonArrayCreateNew() {
        // 确保键不存在
        assertNull(writable.readJsonArray("newArray"));
        
        // 创建新的 JSON 数组
        JsonArray newArray = writable.ensureJsonArray("newArray");
        
        // 验证返回的数组不为空
        assertNotNull(newArray);
        assertTrue(newArray.isEmpty());
        
        // 验证数组已添加到 writable 中
        JsonArray retrievedArray = writable.readJsonArray("newArray");
        assertNotNull(retrievedArray);
        assertTrue(retrievedArray.isEmpty());
        
        getUnitTestLogger().info("成功创建新的 JSON 数组");
    }

    @Test
    @DisplayName("测试 ensureJsonArray 方法 - 获取现有的 JSON 数组")
    void testEnsureJsonArrayGetExisting() {
        // 先添加一个 JSON 数组
        JsonArray originalArray = new JsonArray().add("item1").add("item2").add(3);
        writable.ensureEntry("existingArray", originalArray);
        
        // 获取现有的 JSON 数组
        JsonArray retrievedArray = writable.ensureJsonArray("existingArray");
        
        // 验证返回的是同一个数组
        assertNotNull(retrievedArray);
        assertEquals("item1", retrievedArray.getString(0));
        assertEquals("item2", retrievedArray.getString(1));
        assertEquals(3, retrievedArray.getInteger(2));
        
        getUnitTestLogger().info("成功获取现有的 JSON 数组");
    }

    @Test
    @DisplayName("测试 ensureJsonArray 方法 - 修改返回的数组")
    void testEnsureJsonArrayModifyReturned() {
        // 创建新的 JSON 数组
        JsonArray newArray = writable.ensureJsonArray("modifiableArray");
        
        // 修改返回的数组
        newArray.add("newItem");
        newArray.add(42);
        
        // 验证修改已反映到 writable 中
        JsonArray retrievedArray = writable.readJsonArray("modifiableArray");
        assertEquals("newItem", retrievedArray.getString(0));
        assertEquals(42, retrievedArray.getInteger(1));
        
        getUnitTestLogger().info("成功修改返回的 JSON 数组");
    }

    @Test
    @DisplayName("测试嵌套结构操作")
    void testNestedStructureOperations() {
        // 创建嵌套的 JSON 对象结构
        JsonObject level1 = writable.ensureJsonObject("level1");
        JsonObject level2 = new JsonObject();
        level1.put("level2", level2);
        JsonObject level3 = new JsonObject();
        level2.put("level3", level3);
        
        // 在嵌套结构中添加值
        level3.put("deepValue", "very deep");
        
        // 验证嵌套结构
        assertEquals("very deep", writable.readString("level1", "level2", "level3", "deepValue"));
        
        // 创建嵌套的数组结构
        JsonArray array1 = writable.ensureJsonArray("array1");
        JsonArray array2 = new JsonArray();
        array1.add(array2);
        array2.add("nested item");
        
        // 验证嵌套数组
        JsonArray retrievedArray2 = writable.readJsonArray("array1").getJsonArray(0);
        assertEquals("nested item", retrievedArray2.getString(0));
        
        getUnitTestLogger().info("成功创建和操作嵌套结构");
    }

    @Test
    @DisplayName("测试复杂数据结构的组合操作")
    void testComplexDataStructureOperations() {
        // 创建一个复杂的数据结构
        JsonObject user = writable.ensureJsonObject("user");
        user.put("name", "张三");
        user.put("age", 25);
        
        JsonArray hobbies = new JsonArray();
        user.put("hobbies", hobbies);
        hobbies.add("读书");
        hobbies.add("游泳");
        
        JsonObject address = new JsonObject();
        user.put("address", address);
        address.put("city", "北京");
        address.put("district", "朝阳区");
        
        JsonArray contacts = new JsonArray();
        address.put("contacts", contacts);
        contacts.add("phone: 123456789");
        contacts.add("email: zhangsan@example.com");
        
        // 验证复杂结构
        assertEquals("张三", writable.readString("user", "name"));
        assertEquals(25, writable.readInteger("user", "age"));
        assertEquals("读书", writable.readJsonArray("user", "hobbies").getString(0));
        assertEquals("北京", writable.readString("user", "address", "city"));
        assertEquals("phone: 123456789", writable.readJsonArray("user", "address", "contacts").getString(0));
        
        getUnitTestLogger().info("成功创建和验证复杂数据结构");
    }

    @Test
    @DisplayName("测试边界情况 - 空键")
    void testEdgeCaseEmptyKey() {
        // 测试空键
        writable.ensureEntry("", "empty key value");
        assertEquals("empty key value", writable.readString(""));
        
        // 测试空键的 JSON 对象
        JsonObject emptyKeyObject = writable.ensureJsonObject("");
        assertNotNull(emptyKeyObject);
        
        // 测试空键的 JSON 数组
        JsonArray emptyKeyArray = writable.ensureJsonArray("");
        assertNotNull(emptyKeyArray);
        
        getUnitTestLogger().info("空键操作正常");
    }

    @Test
    @DisplayName("测试边界情况 - null 值")
    void testEdgeCaseNullValue() {
        // 测试 null 值
        writable.ensureEntry("nullKey", null);
        assertNull(writable.readString("nullKey"));
        
        // 测试 null 值的 JSON 对象
        JsonObject nullObject = writable.ensureJsonObject("nullObject");
        nullObject.put("key", null);
        assertNull(nullObject.getString("key"));
        
        getUnitTestLogger().info("null 值操作正常");
    }

    @Test
    @DisplayName("测试性能 - 大量操作")
    void testPerformanceLargeOperations() {
        long startTime = System.currentTimeMillis();
        
        // 执行大量操作
        for (int i = 0; i < 1000; i++) {
            writable.ensureEntry("key" + i, "value" + i);
        }
        
        // 验证所有值都正确添加
        for (int i = 0; i < 1000; i++) {
            assertEquals("value" + i, writable.readString("key" + i));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        getUnitTestLogger().info("完成 1000 次操作，耗时: " + duration + "ms");
        assertTrue(duration < 1000, "操作应该在 1 秒内完成");
    }
}