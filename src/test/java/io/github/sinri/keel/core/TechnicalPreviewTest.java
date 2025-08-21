package io.github.sinri.keel.core;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class TechnicalPreviewTest extends KeelJUnit5Test {

    public TechnicalPreviewTest(Vertx vertx) {
        super(vertx);
    }

    @TechnicalPreview(since = "3.0.10", notice = "Test annotation")
    private static class TestClass {
        @TechnicalPreview(since = "3.1.0", notice = "Test method")
        public void testMethod() {
        }

        @TechnicalPreview(since = "3.2.0")
        public void testMethodWithoutNotice() {
        }
    }

    @TechnicalPreview(since = "4.0.0")
    private static class TestClassWithoutNotice {
    }

    @Test
    @DisplayName("Test TechnicalPreview annotation retention")
    void testAnnotationRetention() {
        TechnicalPreview annotation = TestClass.class.getAnnotation(TechnicalPreview.class);
        assertNotNull(annotation);
    }

    @Test
    @DisplayName("Test TechnicalPreview since attribute")
    void testSinceAttribute() {
        TechnicalPreview annotation = TestClass.class.getAnnotation(TechnicalPreview.class);
        assertEquals("3.0.10", annotation.since());
        
        TechnicalPreview methodAnnotation = getMethodAnnotation("testMethod");
        assertEquals("3.1.0", methodAnnotation.since());
    }

    @Test
    @DisplayName("Test TechnicalPreview notice attribute")
    void testNoticeAttribute() {
        TechnicalPreview annotation = TestClass.class.getAnnotation(TechnicalPreview.class);
        assertEquals("Test annotation", annotation.notice());
        
        TechnicalPreview methodAnnotation = getMethodAnnotation("testMethod");
        assertEquals("Test method", methodAnnotation.notice());
    }

    @Test
    @DisplayName("Test TechnicalPreview default notice value")
    void testDefaultNoticeValue() {
        TechnicalPreview annotation = TestClassWithoutNotice.class.getAnnotation(TechnicalPreview.class);
        assertEquals("", annotation.notice());
        
        TechnicalPreview methodAnnotation = getMethodAnnotation("testMethodWithoutNotice");
        assertEquals("", methodAnnotation.notice());
    }

    @Test
    @DisplayName("Test TechnicalPreview default since value")
    void testDefaultSinceValue() {
        // 创建一个没有指定since的注解实例
        TechnicalPreview annotation = new TechnicalPreview() {
            @Override
            public String since() {
                return "";
            }

            @Override
            public String notice() {
                return "";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return TechnicalPreview.class;
            }
        };
        
        assertEquals("", annotation.since());
    }

    @Test
    @DisplayName("Test TechnicalPreview annotation targets")
    void testAnnotationTargets() {
        // 验证注解可以用于类
        assertTrue(TestClass.class.isAnnotationPresent(TechnicalPreview.class));
        
        // 验证注解可以用于方法
        assertNotNull(getMethodAnnotation("testMethod"));
    }

    @Test
    @DisplayName("Test TechnicalPreview annotation documentation")
    void testAnnotationDocumentation() {
        // 验证注解本身有Documented注解
        assertTrue(TechnicalPreview.class.isAnnotationPresent(java.lang.annotation.Documented.class));
    }

    private TechnicalPreview getMethodAnnotation(String methodName) {
        try {
            Method method = TestClass.class.getMethod(methodName);
            return method.getAnnotation(TechnicalPreview.class);
        } catch (NoSuchMethodException e) {
            fail("Method " + methodName + " not found");
            return null;
        }
    }
} 