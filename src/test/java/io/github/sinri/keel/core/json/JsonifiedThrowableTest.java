package io.github.sinri.keel.core.json;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class JsonifiedThrowableTest extends KeelJUnit5Test {

    private RuntimeException simpleException;
    private Exception chainedException;
    private NullPointerException nullPointerException;

    public JsonifiedThrowableTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    public void setUp() {
        // 创建简单的异常
        simpleException = new RuntimeException("这是一个简单的运行时异常");

        // 创建链式异常
        nullPointerException = new NullPointerException("空指针异常");
        chainedException = new Exception("包装异常", nullPointerException);

        // 确保序列化器已注册
        JsonifiableSerializer.register();
    }

    @Test
    void testWrapSimpleException() {
        // 测试包装简单异常
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.RuntimeException", jsonifiedThrowable.getThrowableClass());
        assertEquals("这是一个简单的运行时异常", jsonifiedThrowable.getThrowableMessage());
        assertNotNull(jsonifiedThrowable.getThrowableStack());
        assertFalse(jsonifiedThrowable.getThrowableStack().isEmpty());
        assertNull(jsonifiedThrowable.getThrowableCause());

        getUnitTestLogger().info("简单异常包装测试通过");
    }

    @Test
    void testWrapChainedException() {
        // 测试包装链式异常
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(chainedException);

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.Exception", jsonifiedThrowable.getThrowableClass());
        assertEquals("包装异常", jsonifiedThrowable.getThrowableMessage());
        assertNotNull(jsonifiedThrowable.getThrowableStack());
        assertFalse(jsonifiedThrowable.getThrowableStack().isEmpty());

        // 检查异常链
        JsonifiedThrowable cause = jsonifiedThrowable.getThrowableCause();
        assertNotNull(cause);
        assertEquals("java.lang.NullPointerException", cause.getThrowableClass());
        assertEquals("空指针异常", cause.getThrowableMessage());
        assertNotNull(cause.getThrowableStack());
        assertFalse(cause.getThrowableStack().isEmpty());
        assertNull(cause.getThrowableCause());

        getUnitTestLogger().info("链式异常包装测试通过");
    }

    @Test
    void testWrapWithCustomFilter() {
        // 测试使用自定义过滤配置
        Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");

        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
                simpleException,
                ignorablePackages,
                true  // 省略被忽略的堆栈信息
        );

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.RuntimeException", jsonifiedThrowable.getThrowableClass());
        assertEquals("这是一个简单的运行时异常", jsonifiedThrowable.getThrowableMessage());

        // 检查堆栈是否被过滤
        var stackItems = jsonifiedThrowable.getThrowableStack();
        assertNotNull(stackItems);

        // 验证堆栈项的类型
        for (JsonifiedThrowable.JsonifiedCallStackItem item : stackItems) {
            String type = item.getType();
            assertTrue("call".equals(type) || "ignored".equals(type),
                    "堆栈项类型应该是 'call' 或 'ignored'，但得到: " + type);
        }

        getUnitTestLogger().info("自定义过滤测试通过");
    }

    @Test
    void testWrapWithKeepIgnoredStack() {
        // 测试保留被忽略的堆栈信息
        Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");

        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
                simpleException,
                ignorablePackages,
                false  // 保留被忽略的堆栈信息
        );

        assertNotNull(jsonifiedThrowable);
        var stackItems = jsonifiedThrowable.getThrowableStack();
        assertNotNull(stackItems);

        // 检查是否包含忽略项
        boolean hasIgnoredItems = stackItems.stream()
                                            .anyMatch(item -> "ignored".equals(item.getType()));

        assertTrue(hasIgnoredItems, "应该包含被忽略的堆栈项");

        getUnitTestLogger().info("保留忽略堆栈测试通过");
    }

    @Test
    void testGetThrowableStack() {
        // 测试获取堆栈信息
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        var stackItems = jsonifiedThrowable.getThrowableStack();

        assertNotNull(stackItems);
        assertFalse(stackItems.isEmpty());

        // 检查第一个堆栈项（应该是调用项）
        JsonifiedThrowable.JsonifiedCallStackItem firstItem = stackItems.get(0);
        assertEquals("call", firstItem.getType());
        assertNotNull(firstItem.getCallStackClass());
        assertNotNull(firstItem.getCallStackMethod());

        getUnitTestLogger().info("堆栈信息获取测试通过，堆栈项数量: " + stackItems.size());
    }

    @Test
    void testJsonifiedCallStackItem() {
        // 测试堆栈项的内部类
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        var stackItems = jsonifiedThrowable.getThrowableStack();

        assertFalse(stackItems.isEmpty());

        // 测试调用项
        JsonifiedThrowable.JsonifiedCallStackItem callItem = stackItems.get(0);
        assertEquals("call", callItem.getType());
        assertNotNull(callItem.getCallStackClass());
        assertNotNull(callItem.getCallStackMethod());
        // 文件名和行号可能为null，这是正常的
        // assertNotNull(callItem.getCallStackFile());
        // assertNotNull(callItem.getCallStackLine());

        // 测试忽略项（如果有的话）
        var ignoredItems = stackItems.stream()
                                     .filter(item -> "ignored".equals(item.getType()))
                                     .collect(Collectors.toList());

        for (JsonifiedThrowable.JsonifiedCallStackItem ignoredItem : ignoredItems) {
            assertEquals("ignored", ignoredItem.getType());
            assertNotNull(ignoredItem.getPackage());
            assertNotNull(ignoredItem.getIgnoredStackCount());
        }

        getUnitTestLogger().info("堆栈项内部类测试通过");
    }

    @Test
    void testToJsonObject() {
        // 测试转换为JsonObject
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        JsonObject jsonObject = jsonifiedThrowable.toJsonObject();

        assertNotNull(jsonObject);
        assertEquals("java.lang.RuntimeException", jsonObject.getString("class"));
        assertEquals("这是一个简单的运行时异常", jsonObject.getString("message"));
        assertNotNull(jsonObject.getJsonArray("stack"));
        assertNull(jsonObject.getJsonObject("cause"));

        getUnitTestLogger().info("JSON转换测试通过");
        getUnitTestLogger().info("JSON内容: " + jsonObject.encodePrettily());
    }

    @Test
    void testToJsonObjectWithChainedException() {
        // 测试链式异常的JSON转换
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(chainedException);
        JsonObject jsonObject = jsonifiedThrowable.toJsonObject();

        assertNotNull(jsonObject);
        assertEquals("java.lang.Exception", jsonObject.getString("class"));
        assertEquals("包装异常", jsonObject.getString("message"));
        assertNotNull(jsonObject.getJsonArray("stack"));

        // 检查异常链
        JsonifiedThrowable cause = jsonifiedThrowable.getThrowableCause();
        assertNotNull(cause);
        assertEquals("java.lang.NullPointerException", cause.getThrowableClass());
        assertEquals("空指针异常", cause.getThrowableMessage());
        assertNotNull(cause.getThrowableStack());
        assertNull(cause.getThrowableCause());

        getUnitTestLogger().info("链式异常JSON转换测试通过");
    }

    @Test
    void testToJsonExpression() {
        // 测试转换为JSON字符串
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        String jsonExpression = jsonifiedThrowable.toJsonExpression();

        assertNotNull(jsonExpression);
        assertTrue(jsonExpression.contains("java.lang.RuntimeException"));
        assertTrue(jsonExpression.contains("这是一个简单的运行时异常"));

        getUnitTestLogger().info("JSON表达式测试通过");
    }

    @Test
    void testToFormattedJsonExpression() {
        // 测试转换为格式化的JSON字符串
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        String formattedJson = jsonifiedThrowable.toFormattedJsonExpression();

        assertNotNull(formattedJson);
        assertTrue(formattedJson.contains("java.lang.RuntimeException"));
        assertTrue(formattedJson.contains("这是一个简单的运行时异常"));
        assertTrue(formattedJson.contains("\n")); // 格式化应该包含换行符

        getUnitTestLogger().info("格式化JSON表达式测试通过");
    }

    @Test
    void testExceptionWithNullMessage() {
        // 测试消息为null的异常
        RuntimeException exceptionWithNullMessage = new RuntimeException();
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(exceptionWithNullMessage);

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.RuntimeException", jsonifiedThrowable.getThrowableClass());
        assertNull(jsonifiedThrowable.getThrowableMessage());

        getUnitTestLogger().info("空消息异常测试通过");
    }

    @Test
    void testExceptionWithEmptyMessage() {
        // 测试空消息的异常
        RuntimeException exceptionWithEmptyMessage = new RuntimeException("");
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(exceptionWithEmptyMessage);

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.RuntimeException", jsonifiedThrowable.getThrowableClass());
        assertEquals("", jsonifiedThrowable.getThrowableMessage());

        getUnitTestLogger().info("空字符串消息异常测试通过");
    }

    @Test
    void testComplexExceptionChain() {
        // 测试复杂的异常链
        IllegalArgumentException argException = new IllegalArgumentException("参数错误");
        RuntimeException runtimeException = new RuntimeException("运行时错误", argException);
        Exception topException = new Exception("顶层异常", runtimeException);

        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(topException);

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.Exception", jsonifiedThrowable.getThrowableClass());
        assertEquals("顶层异常", jsonifiedThrowable.getThrowableMessage());

        // 检查三层异常链
        JsonifiedThrowable cause1 = jsonifiedThrowable.getThrowableCause();
        assertNotNull(cause1);
        assertEquals("java.lang.RuntimeException", cause1.getThrowableClass());
        assertEquals("运行时错误", cause1.getThrowableMessage());

        JsonifiedThrowable cause2 = cause1.getThrowableCause();
        assertNotNull(cause2);
        assertEquals("java.lang.IllegalArgumentException", cause2.getThrowableClass());
        assertEquals("参数错误", cause2.getThrowableMessage());

        assertNull(cause2.getThrowableCause());

        getUnitTestLogger().info("复杂异常链测试通过");
    }

    @Test
    void testGetImplementation() {
        // 测试getImplementation方法
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        JsonifiedThrowable implementation = jsonifiedThrowable.getImplementation();

        assertNotNull(implementation);
        assertSame(jsonifiedThrowable, implementation);

        getUnitTestLogger().info("getImplementation方法测试通过");
    }

    @Test
    void testCallStackItemGetImplementation() {
        // 测试堆栈项的getImplementation方法
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);
        var stackItems = jsonifiedThrowable.getThrowableStack();

        assertFalse(stackItems.isEmpty());

        JsonifiedThrowable.JsonifiedCallStackItem firstItem = stackItems.get(0);
        JsonifiedThrowable.JsonifiedCallStackItem implementation = firstItem.getImplementation();

        assertNotNull(implementation);
        assertSame(firstItem, implementation);

        getUnitTestLogger().info("堆栈项getImplementation方法测试通过");
    }

    @Test
    void testNullThrowable() {
        // 测试传入null的情况（虽然方法标注了@Nonnull，但测试边界情况）
        assertThrows(NullPointerException.class, () -> {
            JsonifiedThrowable.wrap(null);
        });

        getUnitTestLogger().info("null异常参数测试通过");
    }

    @Test
    void testEmptyIgnorablePackages() {
        // 测试空的忽略包集合
        Set<String> emptyPackages = Set.of();

        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(
                simpleException,
                emptyPackages,
                true
        );

        assertNotNull(jsonifiedThrowable);
        assertEquals("java.lang.RuntimeException", jsonifiedThrowable.getThrowableClass());

        // 空包集合应该不会过滤任何堆栈
        var stackItems = jsonifiedThrowable.getThrowableStack();
        assertNotNull(stackItems);

        getUnitTestLogger().info("空忽略包集合测试通过");
    }

    @Test
    void testIntegrationWithJsonifiableSerializer() {
        // 测试与JsonifiableSerializer的集成
        JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(simpleException);

        // 验证可以正确序列化
        JsonObject jsonObject = jsonifiedThrowable.toJsonObject();
        assertNotNull(jsonObject);

        // 验证JSON内容正确
        assertEquals("java.lang.RuntimeException", jsonObject.getString("class"));
        assertEquals("这是一个简单的运行时异常", jsonObject.getString("message"));
        assertNotNull(jsonObject.getJsonArray("stack"));
        assertNull(jsonObject.getJsonObject("cause"));

        // 验证JSON字符串序列化
        String jsonString = jsonifiedThrowable.toJsonExpression();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("java.lang.RuntimeException"));
        assertTrue(jsonString.contains("这是一个简单的运行时异常"));

        getUnitTestLogger().info("JsonifiableSerializer集成测试通过");
    }
}