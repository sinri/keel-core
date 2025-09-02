package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelJsonHelperTest extends KeelJUnit5Test {

    public KeelJsonHelperTest(Vertx vertx) {
        super(vertx);
    }

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
    // 注意：以下测试方法在 KeelJsonHelper 中不存在，已注释掉
    // 实际的 KeelJsonHelper 只提供排序和格式化功能
    
    /*
    @Test
    @DisplayName("测试 JsonObject 基本写入操作")
    void testWriteIntoJsonObject() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonArray 基本写入操作")
    void testWriteIntoJsonArray() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonObject 基本读取操作")
    void testReadFromJsonObject() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonArray 基本读取操作")
    void testReadFromJsonArray() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    // ==================== 键链访问测试 ====================
    // 以下键链操作方法在 KeelJsonHelper 中不存在，已注释掉

    @Test
    @DisplayName("测试 JsonObject 键链写入操作")
    void testWriteIntoJsonObjectWithKeychain() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonArray 键链写入操作")
    void testWriteIntoJsonArrayWithKeychain() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonObject 键链读取操作")
    void testReadFromJsonObjectWithKeychain() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试 JsonArray 键链读取操作")
    void testReadFromJsonArrayWithKeychain() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试键链操作异常情况")
    void testKeychainExceptions() {
        // 此方法在 KeelJsonHelper 中不存在
    }
    */

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
    // 注意：renderThrowableChain 方法在 KeelJsonHelper 中不存在，已注释掉
    
    /*
    @Test
    @DisplayName("测试异常链渲染 - 无过滤")
    void testRenderThrowableChain() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试异常链渲染 - 带过滤")
    void testRenderThrowableChainWithFilter() {
        // 此方法在 KeelJsonHelper 中不存在
    }

    @Test
    @DisplayName("测试空异常渲染")
    void testRenderThrowableChainNull() {
        // 此方法在 KeelJsonHelper 中不存在
    }
    */

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

        // 注意：readFromJsonObject 方法在 KeelJsonHelper 中不存在，所以只测试排序功能
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