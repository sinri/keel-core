package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeelJsonHelperTest extends KeelUnitTest {

    private KeelJsonHelper jsonHelper;

    @BeforeEach
    public void setUp() {
        jsonHelper = KeelJsonHelper.getInstance();
    }

    @Test
    @DisplayName("测试单例模式")
    void testGetInstance() {
        KeelJsonHelper instance1 = KeelJsonHelper.getInstance();
        KeelJsonHelper instance2 = KeelJsonHelper.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    // ==================== 基本读写操作测试 ====================

    @Test
    @DisplayName("测试 JsonObject 基本写入操作")
    void testWriteIntoJsonObject() {
        JsonObject jsonObject = new JsonObject();
        
        // 测试写入不同类型的值
        jsonHelper.writeIntoJsonObject(jsonObject, "string", "test");
        jsonHelper.writeIntoJsonObject(jsonObject, "number", 123);
        jsonHelper.writeIntoJsonObject(jsonObject, "boolean", true);
        jsonHelper.writeIntoJsonObject(jsonObject, "null", null);
        
        assertEquals("test", jsonObject.getString("string"));
        assertEquals(123, jsonObject.getInteger("number"));
        assertEquals(true, jsonObject.getBoolean("boolean"));
        assertNull(jsonObject.getValue("null"));
        
        getUnitTestLogger().info("JsonObject 写入测试通过: " + jsonObject.encodePrettily());
    }

    @Test
    @DisplayName("测试 JsonArray 基本写入操作")
    void testWriteIntoJsonArray() {
        JsonArray jsonArray = new JsonArray();
        
        // 测试负索引（追加到末尾）
        jsonHelper.writeIntoJsonArray(jsonArray, -1, "first");
        assertEquals("first", jsonArray.getString(0));
        
        // 测试指定索引
        jsonHelper.writeIntoJsonArray(jsonArray, 2, "third");
        assertEquals("third", jsonArray.getString(2));
        assertNull(jsonArray.getValue(1)); // 中间位置应该填充 null
        
        // 测试覆盖现有值
        jsonHelper.writeIntoJsonArray(jsonArray, 0, "updated");
        assertEquals("updated", jsonArray.getString(0));
        
        getUnitTestLogger().info("JsonArray 写入测试通过: " + jsonArray.encodePrettily());
    }

    @Test
    @DisplayName("测试 JsonObject 基本读取操作")
    void testReadFromJsonObject() {
        JsonObject jsonObject = new JsonObject()
                .put("string", "test")
                .put("number", 123)
                .put("boolean", true)
                .put("null", null);
        
        assertEquals("test", jsonHelper.readFromJsonObject(jsonObject, "string"));
        assertEquals(123, jsonHelper.readFromJsonObject(jsonObject, "number"));
        assertEquals(true, jsonHelper.readFromJsonObject(jsonObject, "boolean"));
        assertNull(jsonHelper.readFromJsonObject(jsonObject, "null"));
        assertNull(jsonHelper.readFromJsonObject(jsonObject, "nonexistent"));
        
        getUnitTestLogger().info("JsonObject 读取测试通过");
    }

    @Test
    @DisplayName("测试 JsonArray 基本读取操作")
    void testReadFromJsonArray() {
        JsonArray jsonArray = new JsonArray()
                .add("first")
                .add(123)
                .add(true)
                .add(null);
        
        assertEquals("first", jsonHelper.readFromJsonArray(jsonArray, 0));
        assertEquals(123, jsonHelper.readFromJsonArray(jsonArray, 1));
        assertEquals(true, jsonHelper.readFromJsonArray(jsonArray, 2));
        assertNull(jsonHelper.readFromJsonArray(jsonArray, 3));
        // 注意：Vert.x JsonArray 在访问超出范围的索引时会抛出异常，而不是返回 null
        assertThrows(IndexOutOfBoundsException.class, () -> 
            jsonHelper.readFromJsonArray(jsonArray, 10));
        
        getUnitTestLogger().info("JsonArray 读取测试通过");
    }

    // ==================== 键链访问测试 ====================

    @Test
    @DisplayName("测试 JsonObject 键链写入操作")
    void testWriteIntoJsonObjectWithKeychain() {
        JsonObject jsonObject = new JsonObject();
        
        // 测试单层键链
        List<Object> singleKeychain = List.of("user");
        jsonHelper.writeIntoJsonObject(jsonObject, singleKeychain, "john");
        assertEquals("john", jsonObject.getString("user"));
        
        // 测试多层嵌套键链 - 先创建对象结构
        jsonHelper.writeIntoJsonObject(jsonObject, "user", new JsonObject());
        List<Object> nestedKeychain = Arrays.asList("user", "profile", "name");
        jsonHelper.writeIntoJsonObject(jsonObject, nestedKeychain, "John Doe");
        
        JsonObject user = jsonObject.getJsonObject("user");
        assertNotNull(user);
        JsonObject profile = user.getJsonObject("profile");
        assertNotNull(profile);
        assertEquals("John Doe", profile.getString("name"));
        
        // 测试数组索引键链 - 先创建数组结构
        jsonHelper.writeIntoJsonObject(jsonObject, "users", new JsonArray());
        List<Object> arrayKeychain = Arrays.asList("users", 0, "name");
        jsonHelper.writeIntoJsonObject(jsonObject, arrayKeychain, "Alice");
        
        JsonArray users = jsonObject.getJsonArray("users");
        assertNotNull(users);
        JsonObject firstUser = users.getJsonObject(0);
        assertNotNull(firstUser);
        assertEquals("Alice", firstUser.getString("name"));
        
        getUnitTestLogger().info("JsonObject 键链写入测试通过: " + jsonObject.encodePrettily());
    }

    @Test
    @DisplayName("测试 JsonArray 键链写入操作")
    void testWriteIntoJsonArrayWithKeychain() {
        JsonArray jsonArray = new JsonArray();
        
        // 测试单层索引键链
        List<Object> singleKeychain = List.of(0);
        jsonHelper.writeIntoJsonArray(jsonArray, singleKeychain, "first");
        assertEquals("first", jsonArray.getString(0));
        
        // 测试多层嵌套键链 - 先创建对象结构
        jsonHelper.writeIntoJsonArray(jsonArray, 1, new JsonObject());
        List<Object> nestedKeychain = Arrays.asList(1, "profile", "name");
        jsonHelper.writeIntoJsonArray(jsonArray, nestedKeychain, "John Doe");
        
        JsonObject firstItem = jsonArray.getJsonObject(1);
        assertNotNull(firstItem);
        JsonObject profile = firstItem.getJsonObject("profile");
        assertNotNull(profile);
        assertEquals("John Doe", profile.getString("name"));
        
        // 测试数组嵌套键链 - 先创建数组结构
        jsonHelper.writeIntoJsonArray(jsonArray, 2, new JsonArray());
        List<Object> arrayNestedKeychain = Arrays.asList(2, 0, "name");
        jsonHelper.writeIntoJsonArray(jsonArray, arrayNestedKeychain, "Alice");
        
        JsonArray secondItem = jsonArray.getJsonArray(2);
        assertNotNull(secondItem);
        JsonObject firstInArray = secondItem.getJsonObject(0);
        assertNotNull(firstInArray);
        assertEquals("Alice", firstInArray.getString("name"));
        
        getUnitTestLogger().info("JsonArray 键链写入测试通过: " + jsonArray.encodePrettily());
    }

    @Test
    @DisplayName("测试 JsonObject 键链读取操作")
    void testReadFromJsonObjectWithKeychain() {
        JsonObject jsonObject = new JsonObject()
                .put("user", new JsonObject()
                        .put("profile", new JsonObject()
                                .put("name", "John Doe")
                                .put("age", 30))
                        .put("settings", new JsonObject()
                                .put("theme", "dark")))
                .put("users", new JsonArray()
                        .add(new JsonObject().put("name", "Alice"))
                        .add(new JsonObject().put("name", "Bob")));
        
        // 测试单层键链
        assertEquals("John Doe", jsonHelper.readFromJsonObject(jsonObject, Arrays.asList("user", "profile", "name")));
        assertEquals(30, jsonHelper.readFromJsonObject(jsonObject, Arrays.asList("user", "profile", "age")));
        assertEquals("dark", jsonHelper.readFromJsonObject(jsonObject, Arrays.asList("user", "settings", "theme")));
        
        // 测试数组访问
        assertEquals("Alice", jsonHelper.readFromJsonObject(jsonObject, Arrays.asList("users", 0, "name")));
        assertEquals("Bob", jsonHelper.readFromJsonObject(jsonObject, Arrays.asList("users", 1, "name")));
        
        getUnitTestLogger().info("JsonObject 键链读取测试通过");
    }

    @Test
    @DisplayName("测试 JsonArray 键链读取操作")
    void testReadFromJsonArrayWithKeychain() {
        JsonArray jsonArray = new JsonArray()
                .add(new JsonObject()
                        .put("profile", new JsonObject()
                                .put("name", "John Doe")))
                .add(new JsonArray()
                        .add(new JsonObject().put("name", "Alice"))
                        .add(new JsonObject().put("name", "Bob")));
        
        // 测试对象嵌套
        assertEquals("John Doe", jsonHelper.readFromJsonArray(jsonArray, Arrays.asList(0, "profile", "name")));
        
        // 测试数组嵌套
        assertEquals("Alice", jsonHelper.readFromJsonArray(jsonArray, Arrays.asList(1, 0, "name")));
        assertEquals("Bob", jsonHelper.readFromJsonArray(jsonArray, Arrays.asList(1, 1, "name")));
        
        getUnitTestLogger().info("JsonArray 键链读取测试通过");
    }

    @Test
    @DisplayName("测试键链操作异常情况")
    void testKeychainExceptions() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        
        // 测试空键链
        assertThrows(RuntimeException.class, () -> 
            jsonHelper.writeIntoJsonObject(jsonObject, List.of(), "value"));
        assertThrows(RuntimeException.class, () -> 
            jsonHelper.readFromJsonObject(jsonObject, List.of()));
        
        // 测试数组键链中的非数字键
        assertThrows(RuntimeException.class, () -> 
            jsonHelper.writeIntoJsonArray(jsonArray, List.of("invalid"), "value"));
        assertThrows(RuntimeException.class, () -> 
            jsonHelper.readFromJsonArray(jsonArray, List.of("invalid")));
        
        getUnitTestLogger().info("键链异常测试通过");
    }

    // ==================== 排序功能测试 ====================

    @Test
    @DisplayName("测试 JsonArray 排序功能")
    void testGetJsonForArrayWhoseItemsSorted() {
        JsonArray unsortedArray = new JsonArray()
                .add("zebra")
                .add("apple")
                .add("banana")
                .add("cherry");
        
        String sortedJson = jsonHelper.getJsonForArrayWhoseItemsSorted(unsortedArray);
        
        // 验证排序结果
        JsonArray sortedArray = new JsonArray(sortedJson);
        assertEquals("apple", sortedArray.getString(0));
        assertEquals("banana", sortedArray.getString(1));
        assertEquals("cherry", sortedArray.getString(2));
        assertEquals("zebra", sortedArray.getString(3));
        
        getUnitTestLogger().info("JsonArray 排序测试通过: " + sortedJson);
    }

    @Test
    @DisplayName("测试 JsonObject 排序功能")
    void testGetJsonForObjectWhoseItemKeysSorted() {
        JsonObject unsortedObject = new JsonObject()
                .put("zebra", "last")
                .put("apple", "first")
                .put("banana", "second")
                .put("cherry", "third");
        
        String sortedJson = jsonHelper.getJsonForObjectWhoseItemKeysSorted(unsortedObject);
        
        // 验证排序结果
        JsonObject sortedObject = new JsonObject(sortedJson);
        String[] expectedKeys = {"apple", "banana", "cherry", "zebra"};
        int index = 0;
        for (String key : sortedObject.getMap().keySet()) {
            assertEquals(expectedKeys[index], key);
            index++;
        }
        
        getUnitTestLogger().info("JsonObject 排序测试通过: " + sortedJson);
    }

    @Test
    @DisplayName("测试嵌套结构排序功能")
    void testNestedStructureSorting() {
        JsonObject nestedObject = new JsonObject()
                .put("zebra", new JsonObject()
                        .put("c", 3)
                        .put("a", 1)
                        .put("b", 2))
                .put("apple", new JsonArray()
                        .add("z")
                        .add("a")
                        .add("m"));
        
        String sortedJson = jsonHelper.getJsonForObjectWhoseItemKeysSorted(nestedObject);
        
        // 验证外层排序
        JsonObject sortedObject = new JsonObject(sortedJson);
        String[] expectedOuterKeys = {"apple", "zebra"};
        int index = 0;
        for (String key : sortedObject.getMap().keySet()) {
            assertEquals(expectedOuterKeys[index], key);
            index++;
        }
        
        // 验证内层排序
        JsonObject zebra = sortedObject.getJsonObject("zebra");
        String[] expectedInnerKeys = {"a", "b", "c"};
        index = 0;
        for (String key : zebra.getMap().keySet()) {
            assertEquals(expectedInnerKeys[index], key);
            index++;
        }
        
        getUnitTestLogger().info("嵌套结构排序测试通过: " + sortedJson);
    }

    // ==================== 异常处理测试 ====================

    @Test
    @DisplayName("测试异常链渲染 - 无过滤")
    void testRenderThrowableChain() {
        Exception cause = new RuntimeException("Root cause");
        Exception exception = new IllegalArgumentException("Test exception", cause);
        
        JsonObject exceptionJson = jsonHelper.renderThrowableChain(exception);
        
        assertNotNull(exceptionJson);
        assertEquals("java.lang.IllegalArgumentException", exceptionJson.getString("class"));
        assertEquals("Test exception", exceptionJson.getString("message"));
        
        JsonArray stack = exceptionJson.getJsonArray("stack");
        assertNotNull(stack);
        assertFalse(stack.isEmpty());
        
        JsonObject causeJson = exceptionJson.getJsonObject("cause");
        assertNotNull(causeJson);
        assertEquals("java.lang.RuntimeException", causeJson.getString("class"));
        assertEquals("Root cause", causeJson.getString("message"));
        
        getUnitTestLogger().info("异常链渲染测试通过: " + exceptionJson.encodePrettily());
    }

    @Test
    @DisplayName("测试异常链渲染 - 带过滤")
    void testRenderThrowableChainWithFilter() {
        Exception exception = new RuntimeException("Test exception");
        
        Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");
        JsonObject exceptionJson = jsonHelper.renderThrowableChain(exception, ignorablePackages);
        
        assertNotNull(exceptionJson);
        assertEquals("java.lang.RuntimeException", exceptionJson.getString("class"));
        assertEquals("Test exception", exceptionJson.getString("message"));
        
        JsonArray stack = exceptionJson.getJsonArray("stack");
        assertNotNull(stack);
        
        // 验证过滤结果
        boolean hasIgnoredEntry = false;
        boolean hasCallEntry = false;
        for (int i = 0; i < stack.size(); i++) {
            JsonObject entry = stack.getJsonObject(i);
            String type = entry.getString("type");
            if ("ignored".equals(type)) {
                hasIgnoredEntry = true;
                assertTrue(ignorablePackages.contains(entry.getString("package")));
            } else if ("call".equals(type)) {
                hasCallEntry = true;
                assertNotNull(entry.getString("class"));
                assertNotNull(entry.getString("method"));
            }
        }
        
        // 至少应该有一些调用条目
        assertTrue(hasCallEntry);
        
        getUnitTestLogger().info("异常链渲染过滤测试通过: " + exceptionJson.encodePrettily());
    }

    @Test
    @DisplayName("测试空异常渲染")
    void testRenderThrowableChainNull() {
        JsonObject result = jsonHelper.renderThrowableChain(null);
        assertNull(result);
        
        JsonObject resultWithFilter = jsonHelper.renderThrowableChain(null, Set.of("java.lang"));
        assertNull(resultWithFilter);
        
        getUnitTestLogger().info("空异常渲染测试通过");
    }

    // ==================== 格式化输出测试 ====================

    @Test
    @DisplayName("测试 JSON 块状格式化输出")
    void testRenderJsonToStringBlock() {
        JsonObject testObject = new JsonObject()
                .put("name", "John Doe")
                .put("age", 30)
                .put("active", true)
                .put("profile", new JsonObject()
                        .put("email", "john@example.com")
                        .put("phone", "123-456-7890"))
                .put("hobbies", new JsonArray()
                        .add("reading")
                        .add("swimming")
                        .add("coding"));
        
        String blockFormat = jsonHelper.renderJsonToStringBlock("User", testObject);
        
        assertNotNull(blockFormat);
        assertTrue(blockFormat.contains("User:"));
        assertTrue(blockFormat.contains("name: John Doe"));
        assertTrue(blockFormat.contains("age: 30"));
        assertTrue(blockFormat.contains("active: true"));
        assertTrue(blockFormat.contains("email: john@example.com"));
        assertTrue(blockFormat.contains("phone: 123-456-7890"));
        assertTrue(blockFormat.contains("0: reading"));
        assertTrue(blockFormat.contains("1: swimming"));
        assertTrue(blockFormat.contains("2: coding"));
        
        getUnitTestLogger().info("JSON 块状格式化测试通过:\n" + blockFormat);
    }

    @Test
    @DisplayName("测试空值格式化输出")
    void testRenderJsonToStringBlockNull() {
        String result = jsonHelper.renderJsonToStringBlock(null, null);
        assertEquals("null", result);
        
        String resultWithName = jsonHelper.renderJsonToStringBlock("Test", null);
        assertEquals("null", resultWithName);
        
        getUnitTestLogger().info("空值格式化测试通过");
    }

    @Test
    @DisplayName("测试复杂嵌套结构格式化输出")
    void testRenderJsonToStringBlockComplex() {
        JsonObject complexObject = new JsonObject()
                .put("application", new JsonObject()
                        .put("name", "MyApp")
                        .put("version", "1.0.0")
                        .put("config", new JsonObject()
                                .put("debug", true)
                                .put("port", 8080)))
                .put("users", new JsonArray()
                        .add(new JsonObject()
                                .put("id", 1)
                                .put("name", "Alice")
                                .put("roles", new JsonArray().add("admin").add("user")))
                        .add(new JsonObject()
                                .put("id", 2)
                                .put("name", "Bob")
                                .put("roles", new JsonArray().add("user"))));
        
        String blockFormat = jsonHelper.renderJsonToStringBlock("Application", complexObject);
        
        assertNotNull(blockFormat);
        assertTrue(blockFormat.contains("Application:"));
        assertTrue(blockFormat.contains("name: MyApp"));
        assertTrue(blockFormat.contains("version: 1.0.0"));
        assertTrue(blockFormat.contains("debug: true"));
        assertTrue(blockFormat.contains("port: 8080"));
        assertTrue(blockFormat.contains("id: 1"));
        assertTrue(blockFormat.contains("id: 2"));
        assertTrue(blockFormat.contains("name: Alice"));
        assertTrue(blockFormat.contains("name: Bob"));
        assertTrue(blockFormat.contains("0: admin"));
        assertTrue(blockFormat.contains("1: user"));
        
        getUnitTestLogger().info("复杂嵌套结构格式化测试通过:\n" + blockFormat);
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("测试大数组性能")
    void testLargeArrayPerformance() {
        JsonArray largeArray = new JsonArray();
        for (int i = 0; i < 1000; i++) {
            largeArray.add("item" + i);
        }
        
        long startTime = System.currentTimeMillis();
        String sortedJson = jsonHelper.getJsonForArrayWhoseItemsSorted(largeArray);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(sortedJson);
        assertTrue((endTime - startTime) < 1000); // 应该在1秒内完成
        
        getUnitTestLogger().info("大数组排序性能测试通过，耗时: " + (endTime - startTime) + "ms");
    }

    @Test
    @DisplayName("测试特殊字符处理")
    void testSpecialCharacters() {
        JsonObject specialObject = new JsonObject()
                .put("unicode", "测试中文")
                .put("emoji", "😀🎉🚀")
                .put("special", "!@#$%^&*()")
                .put("newline", "line1\nline2")
                .put("tab", "col1\tcol2");
        
        String blockFormat = jsonHelper.renderJsonToStringBlock("Special", specialObject);
        
        assertNotNull(blockFormat);
        assertTrue(blockFormat.contains("unicode: 测试中文"));
        assertTrue(blockFormat.contains("emoji: 😀🎉🚀"));
        assertTrue(blockFormat.contains("special: !@#$%^&*()"));
        
        getUnitTestLogger().info("特殊字符处理测试通过:\n" + blockFormat);
    }

    @Test
    @DisplayName("测试深度嵌套结构")
    void testDeepNestedStructure() {
        // 创建深度嵌套的 JSON 结构
        JsonObject deepObject = new JsonObject();
        JsonObject current = deepObject;
        
        // 创建5层嵌套
        for (int i = 0; i < 5; i++) {
            current.put("level" + i, new JsonObject());
            current = current.getJsonObject("level" + i);
        }
        current.put("value", "deep_value");
        
        // 测试键链访问
        Object result = jsonHelper.readFromJsonObject(deepObject, 
            Arrays.asList("level0", "level1", "level2", "level3", "level4", "value"));
        assertEquals("deep_value", result);
        
        // 测试排序功能
        String sortedJson = jsonHelper.getJsonForObjectWhoseItemKeysSorted(deepObject);
        assertNotNull(sortedJson);
        
        getUnitTestLogger().info("深度嵌套结构测试通过");
    }

    @Test
    @DisplayName("测试混合数据类型排序")
    void testMixedDataTypeSorting() {
        JsonArray mixedArray = new JsonArray()
                .add(3)
                .add("apple")
                .add(1)
                .add("zebra")
                .add(2)
                .add("banana");
        
        String sortedJson = jsonHelper.getJsonForArrayWhoseItemsSorted(mixedArray);
        
        // 验证排序结果（按字符串排序）
        JsonArray sortedArray = new JsonArray(sortedJson);
        assertEquals("1", sortedArray.getString(0));
        assertEquals("2", sortedArray.getString(1));
        assertEquals("3", sortedArray.getString(2));
        assertEquals("apple", sortedArray.getString(3));
        assertEquals("banana", sortedArray.getString(4));
        assertEquals("zebra", sortedArray.getString(5));
        
        getUnitTestLogger().info("混合数据类型排序测试通过: " + sortedJson);
    }
}