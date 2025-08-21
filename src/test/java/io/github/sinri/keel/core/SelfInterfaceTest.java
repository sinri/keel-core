package io.github.sinri.keel.core;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class SelfInterfaceTest extends KeelJUnit5Test {

    public SelfInterfaceTest(Vertx vertx) {
        super(vertx);
    }

    // 测试实现类
    private static class TestImplementation implements SelfInterface<TestImplementation> {
        private final String name;

        public TestImplementation(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public TestImplementation getImplementation() {
            return this;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    @DisplayName("Test SelfInterface implementation returns correct instance")
    void testGetImplementation() {
        TestImplementation impl = new TestImplementation("test");
        
        // 验证getImplementation返回正确的实例
        assertSame(impl, impl.getImplementation());
        
        // 验证返回的实例可以正常使用
        TestImplementation returned = impl.getImplementation();
        assertEquals("test", returned.getName());
    }

    @Test
    @DisplayName("Test SelfInterface method chaining")
    void testMethodChaining() {
        TestImplementation impl = new TestImplementation("test");
        
        // 测试方法链式调用
        TestImplementation result = impl.getImplementation().getImplementation();
        
        assertSame(impl, result);
        assertEquals("test", result.getName());
    }

    @Test
    @DisplayName("Test SelfInterface with multiple instances")
    void testMultipleInstances() {
        TestImplementation impl1 = new TestImplementation("first");
        TestImplementation impl2 = new TestImplementation("second");
        
        // 验证每个实例返回自己
        assertSame(impl1, impl1.getImplementation());
        assertSame(impl2, impl2.getImplementation());
        
        // 验证不同实例不相等
        assertNotSame(impl1.getImplementation(), impl2.getImplementation());
        assertNotEquals(impl1.getName(), impl2.getName());
    }

    @Test
    @DisplayName("Test SelfInterface annotation presence")
    void testAnnotationPresence() {
        // 验证接口上的注解
        assertTrue(SelfInterface.class.isInterface());
        
        // 验证getImplementation方法的注解
        try {
            var method = SelfInterface.class.getMethod("getImplementation");
            assertNotNull(method.getAnnotation(javax.annotation.Nonnull.class));
        } catch (NoSuchMethodException e) {
            fail("getImplementation method not found");
        }
    }
} 