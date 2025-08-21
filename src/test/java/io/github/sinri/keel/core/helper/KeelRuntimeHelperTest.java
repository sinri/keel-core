package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.core.helper.runtime.CPUTimeResult;
import io.github.sinri.keel.core.helper.runtime.GCStatResult;
import io.github.sinri.keel.core.helper.runtime.JVMMemoryResult;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.management.MemoryUsage;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelRuntimeHelperTest extends KeelJUnit5Test {

    public KeelRuntimeHelperTest(Vertx vertx) {
        super(vertx);
    }

    private KeelRuntimeHelper runtimeHelper;

    @BeforeEach
    public void setUp() {
        runtimeHelper = KeelRuntimeHelper.getInstance();
    }

    @Test
    @DisplayName("Test singleton instance")
    void testSingletonInstance() {
        KeelRuntimeHelper instance1 = KeelRuntimeHelper.getInstance();
        KeelRuntimeHelper instance2 = KeelRuntimeHelper.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Test ignorableCallStackPackage set")
    void testIgnorableCallStackPackage() {
        assertNotNull(KeelRuntimeHelper.ignorableCallStackPackage);
        assertFalse(KeelRuntimeHelper.ignorableCallStackPackage.isEmpty());
        
        // 验证包含预期的包名
        assertTrue(KeelRuntimeHelper.ignorableCallStackPackage.contains("io.github.sinri.keel.facade.async."));
        assertTrue(KeelRuntimeHelper.ignorableCallStackPackage.contains("io.vertx.core."));
        assertTrue(KeelRuntimeHelper.ignorableCallStackPackage.contains("java.lang."));
    }

    @Test
    @DisplayName("Test getGCSnapshot")
    void testGetGCSnapshot() {
        GCStatResult result = runtimeHelper.getGCSnapshot();
        
        assertNotNull(result);
        // GC统计结果应该包含一些基本信息
        assertTrue(result.getMinorGCCount() >= 0);
        assertTrue(result.getMajorGCCount() >= 0);
        assertTrue(result.getMinorGCTime() >= 0);
        assertTrue(result.getMajorGCTime() >= 0);
    }

    @Test
    @DisplayName("Test getCPUTimeSnapshot")
    void testGetCPUTimeSnapshot() {
        CPUTimeResult result = runtimeHelper.getCPUTimeSnapshot();
        
        assertNotNull(result);
        // CPU时间应该包含各种状态的时间
        assertTrue(result.getSpentInUserState() >= 0);
        assertTrue(result.getSpentInSystemState() >= 0);
        assertTrue(result.getSpentInIdleState() >= 0);
    }

    @Test
    @DisplayName("Test makeJVMMemorySnapshot")
    void testMakeJVMMemorySnapshot() {
        JVMMemoryResult result = runtimeHelper.makeJVMMemorySnapshot();
        
        assertNotNull(result);
        // 内存信息应该包含物理内存和JVM内存信息
        assertTrue(result.getPhysicalMaxBytes() > 0);
        assertTrue(result.getRuntimeHeapMaxBytes() > 0);
        assertTrue(result.getRuntimeHeapAllocatedBytes() > 0);
        
        // 使用的内存应该小于等于总内存
        assertTrue(result.getRuntimeHeapUsedBytes() <= result.getRuntimeHeapAllocatedBytes());
        assertTrue(result.getPhysicalUsedBytes() <= result.getPhysicalMaxBytes());
    }

    @Test
    @DisplayName("Test getSystemLoadAverage")
    void testGetSystemLoadAverage() {
        double loadAverage = runtimeHelper.getSystemLoadAverage();
        
        // 系统负载平均值可能为负数（不可用）或非负数
        // 在大多数系统上应该是可用的
        assertTrue(loadAverage >= -1.0);
    }

    @Test
    @DisplayName("Test getHeapMemoryUsage")
    void testGetHeapMemoryUsage() {
        MemoryUsage heapUsage = runtimeHelper.getHeapMemoryUsage();
        
        assertNotNull(heapUsage);
        assertTrue(heapUsage.getMax() > 0);
        assertTrue(heapUsage.getCommitted() > 0);
        assertTrue(heapUsage.getUsed() >= 0);
        
        // 使用的内存应该小于等于已提交的内存
        assertTrue(heapUsage.getUsed() <= heapUsage.getCommitted());
        // 已提交的内存应该小于等于最大内存
        assertTrue(heapUsage.getCommitted() <= heapUsage.getMax());
    }

    @Test
    @DisplayName("Test getNonHeapMemoryUsage")
    void testGetNonHeapMemoryUsage() {
        MemoryUsage nonHeapUsage = runtimeHelper.getNonHeapMemoryUsage();
        
        assertNotNull(nonHeapUsage);
        assertTrue(nonHeapUsage.getCommitted() > 0);
        assertTrue(nonHeapUsage.getUsed() >= 0);
        
        // 使用的内存应该小于等于已提交的内存
        assertTrue(nonHeapUsage.getUsed() <= nonHeapUsage.getCommitted());
    }

    @Test
    @DisplayName("Test getObjectPendingFinalizationCount")
    void testGetObjectPendingFinalizationCount() {
        int count = runtimeHelper.getObjectPendingFinalizationCount();
        
        // 等待终结的对象数量应该非负
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("Test measureObjectSizeWithJOL")
    void testMeasureObjectSizeWithJOL() {
        // 测试简单对象
        String testString = "Hello World";
        long stringSize = runtimeHelper.measureObjectSizeWithJOL(testString);
        assertTrue(stringSize > 0);
        
        // 测试整数对象
        Integer testInteger = 42;
        long integerSize = runtimeHelper.measureObjectSizeWithJOL(testInteger);
        assertTrue(integerSize > 0);
        
        // 测试自定义对象
        TestObject testObject = new TestObject("test", 123);
        long objectSize = runtimeHelper.measureObjectSizeWithJOL(testObject);
        assertTrue(objectSize > 0);
    }

    @Test
    @DisplayName("Test calculateObjectDeepSizeWithJOL")
    void testCalculateObjectDeepSizeWithJOL() {
        // 测试简单对象
        String testString = "Hello World";
        long stringSize = runtimeHelper.calculateObjectDeepSizeWithJOL(testString);
        assertTrue(stringSize > 0);
        
        // 测试包含引用的对象
        TestObject testObject = new TestObject("test", 123);
        long objectSize = runtimeHelper.calculateObjectDeepSizeWithJOL(testObject);
        assertTrue(objectSize > 0);
        
        // 深度大小应该大于等于浅层大小
        long shallowSize = runtimeHelper.measureObjectSizeWithJOL(testObject);
        assertTrue(objectSize >= shallowSize);
    }

    @Test
    @DisplayName("Test calculateObjectDeepSizeWithJOL with null")
    void testCalculateObjectDeepSizeWithJOLWithNull() {
        long size = runtimeHelper.calculateObjectDeepSizeWithJOL(null);
        assertEquals(0, size);
    }

    @Test
    @DisplayName("Test calculateObjectDeepSizeWithJOL with circular reference")
    void testCalculateObjectDeepSizeWithJOLWithCircularReference() {
        CircularObject obj1 = new CircularObject("obj1");
        CircularObject obj2 = new CircularObject("obj2");
        obj1.setReference(obj2);
        obj2.setReference(obj1);
        
        // 应该能够处理循环引用而不抛出异常
        long size = runtimeHelper.calculateObjectDeepSizeWithJOL(obj1);
        assertTrue(size > 0);
    }

    // 测试用的内部类
    private static class TestObject {
        private final String name;
        private final int value;
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
    
    private static class CircularObject {
        private final String name;
        private CircularObject reference;
        
        public CircularObject(String name) {
            this.name = name;
        }
        
        public void setReference(CircularObject reference) {
            this.reference = reference;
        }

        public String getName() {
            return name;
        }

        public CircularObject getReference() {
            return reference;
        }
    }
} 