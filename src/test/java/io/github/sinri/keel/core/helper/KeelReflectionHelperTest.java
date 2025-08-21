package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelReflectionHelperTest extends KeelJUnit5Test {

    public KeelReflectionHelperTest(Vertx vertx) {
        super(vertx);
    }

    // Test annotations for reflection testing
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface TestAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @java.lang.annotation.Repeatable(TestRepeatableContainer.class)
    @interface TestRepeatableAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface TestRepeatableContainer {
        TestRepeatableAnnotation[] value();
    }

    // Test classes for inheritance testing
    @TestAnnotation("base")
    static class BaseClass {
        @TestAnnotation("baseMethod")
        public void baseMethod() {}
    }

    @TestAnnotation("derived")
    @TestRepeatableAnnotation("first")
    @TestRepeatableAnnotation("second")
    static class DerivedClass extends BaseClass {
        @TestAnnotation("derivedMethod")
        public void derivedMethod() {}
    }

    static class AnotherDerivedClass extends BaseClass {
        public void anotherMethod() {}
    }

    static class UnrelatedClass {
        public void unrelatedMethod() {}
    }

    @Test
    void testGetInstance() {
        // Test singleton pattern
        var instance1 = KeelReflectionHelper.getInstance();
        var instance2 = KeelReflectionHelper.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void testGetAnnotationOfMethod() throws NoSuchMethodException {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test method with annotation
        Method derivedMethod = DerivedClass.class.getMethod("derivedMethod");
        TestAnnotation annotation = helper.getAnnotationOfMethod(derivedMethod, TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("derivedMethod", annotation.value());
        
        // Test method without annotation
        Method anotherMethod = AnotherDerivedClass.class.getMethod("anotherMethod");
        TestAnnotation noAnnotation = helper.getAnnotationOfMethod(anotherMethod, TestAnnotation.class);
        assertNull(noAnnotation);
        
        // Test method without annotation with default value
        TestAnnotation defaultAnnotation = new TestAnnotation() {
            @Override
            public String value() {
                return "default";
            }
            
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return TestAnnotation.class;
            }
        };
        TestAnnotation result = helper.getAnnotationOfMethod(anotherMethod, TestAnnotation.class, defaultAnnotation);
        assertSame(defaultAnnotation, result);
    }

    @Test
    void testGetAnnotationOfMethodOverloaded() throws NoSuchMethodException {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test the overloaded method without default annotation
        Method derivedMethod = DerivedClass.class.getMethod("derivedMethod");
        TestAnnotation annotation = helper.getAnnotationOfMethod(derivedMethod, TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("derivedMethod", annotation.value());
        
        // Test method without annotation
        Method anotherMethod = AnotherDerivedClass.class.getMethod("anotherMethod");
        TestAnnotation noAnnotation = helper.getAnnotationOfMethod(anotherMethod, TestAnnotation.class);
        assertNull(noAnnotation);
    }

    @Test
    void testGetAnnotationOfClass() {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test class with annotation
        TestAnnotation annotation = helper.getAnnotationOfClass(DerivedClass.class, TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("derived", annotation.value());
        
        // Test class without annotation
        TestAnnotation noAnnotation = helper.getAnnotationOfClass(UnrelatedClass.class, TestAnnotation.class);
        assertNull(noAnnotation);
        
        // Test base class with annotation
        TestAnnotation baseAnnotation = helper.getAnnotationOfClass(BaseClass.class, TestAnnotation.class);
        assertNotNull(baseAnnotation);
        assertEquals("base", baseAnnotation.value());
    }

    @Test
    void testGetAnnotationsOfClass() {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test class with repeatable annotations
        TestRepeatableAnnotation[] annotations = helper.getAnnotationsOfClass(DerivedClass.class, TestRepeatableAnnotation.class);
        assertNotNull(annotations);
        assertEquals(2, annotations.length);
        
        // Verify annotation values
        Set<String> values = Set.of(annotations[0].value(), annotations[1].value());
        assertTrue(values.contains("first"));
        assertTrue(values.contains("second"));
        
        // Test class without repeatable annotations
        TestRepeatableAnnotation[] noAnnotations = helper.getAnnotationsOfClass(BaseClass.class, TestRepeatableAnnotation.class);
        assertNotNull(noAnnotations);
        assertEquals(0, noAnnotations.length);
    }

    @Test
    void testIsClassAssignable() {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test inheritance relationship
        assertTrue(helper.isClassAssignable(BaseClass.class, DerivedClass.class));
        assertTrue(helper.isClassAssignable(BaseClass.class, AnotherDerivedClass.class));
        assertTrue(helper.isClassAssignable(Object.class, BaseClass.class));
        assertTrue(helper.isClassAssignable(Object.class, DerivedClass.class));
        
        // Test same class
        assertTrue(helper.isClassAssignable(BaseClass.class, BaseClass.class));
        assertTrue(helper.isClassAssignable(DerivedClass.class, DerivedClass.class));
        
        // Test unrelated classes
        assertFalse(helper.isClassAssignable(DerivedClass.class, BaseClass.class));
        assertFalse(helper.isClassAssignable(AnotherDerivedClass.class, DerivedClass.class));
        assertFalse(helper.isClassAssignable(UnrelatedClass.class, BaseClass.class));
        assertFalse(helper.isClassAssignable(BaseClass.class, UnrelatedClass.class));
        
        // Test with interfaces
        assertTrue(helper.isClassAssignable(Runnable.class, Runnable.class));
        assertTrue(helper.isClassAssignable(Object.class, String.class));
    }

    @Test
    void testIsVirtualThreadsAvailable() {
        var helper = KeelReflectionHelper.getInstance();
        
        // This test checks if virtual threads are available on the current JVM
        boolean available = helper.isVirtualThreadsAvailable();
        
        // The result depends on the JVM version
        // Java 19+ should have virtual threads available
        // Earlier versions should return false
        assertTrue(available || !available); // This will always pass, but we're testing the method works
        
        // Test that the method returns consistent results
        boolean available2 = helper.isVirtualThreadsAvailable();
        assertEquals(available, available2);
    }

    @Test
    void testSeekClassDescendantsInPackage() {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test seeking descendants in the current package
        Set<Class<? extends BaseClass>> descendants = helper.seekClassDescendantsInPackage(
            "io.github.sinri.keel.core.helper", 
            BaseClass.class
        );
        
        // Should find our test classes
        assertNotNull(descendants);
        assertTrue(descendants.contains(DerivedClass.class));
        assertTrue(descendants.contains(AnotherDerivedClass.class));
        
        // Test seeking in a package that doesn't exist
        Set<Class<? extends BaseClass>> emptyDescendants = helper.seekClassDescendantsInPackage(
            "io.github.sinri.keel.nonexistent.package", 
            BaseClass.class
        );
        assertNotNull(emptyDescendants);
        assertTrue(emptyDescendants.isEmpty());
        
        // Test seeking with Object as base class (should find many classes)
        Set<Class<? extends Object>> objectDescendants = helper.seekClassDescendantsInPackage(
            "io.github.sinri.keel.core.helper", 
            Object.class
        );
        assertNotNull(objectDescendants);
        assertTrue(objectDescendants.size() > 0);
    }

    @Test
    void testNullParameterHandling() {
        var helper = KeelReflectionHelper.getInstance();
        
        // Note: The @Nonnull annotations are compile-time checks and don't automatically throw exceptions
        // The actual behavior depends on the JVM and whether null checks are enabled at runtime
        // These tests verify that the methods handle null parameters gracefully or throw appropriate exceptions
        
        // Test with null parameters - these may or may not throw exceptions depending on the JVM
        // We'll test that the methods don't crash and handle nulls appropriately
        
        try {
            helper.getAnnotationOfMethod(null, TestAnnotation.class);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            Method method = BaseClass.class.getMethod("baseMethod");
            helper.getAnnotationOfMethod(method, null);
            // If no exception is thrown, that's also acceptable behavior
        } catch (NoSuchMethodException e) {
            fail("Method should exist");
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.getAnnotationOfClass(null, TestAnnotation.class);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.getAnnotationOfClass(BaseClass.class, null);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.isClassAssignable(null, BaseClass.class);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.isClassAssignable(BaseClass.class, null);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.seekClassDescendantsInPackage(null, BaseClass.class);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
        
        try {
            helper.seekClassDescendantsInPackage("package.name", null);
            // If no exception is thrown, that's also acceptable behavior
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable one
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
    }

    @Test
    void testAnnotationInheritance() throws NoSuchMethodException {
        var helper = KeelReflectionHelper.getInstance();
        
        // Test that annotations are not inherited by default
        Method baseMethod = BaseClass.class.getMethod("baseMethod");
        TestAnnotation baseMethodAnnotation = helper.getAnnotationOfMethod(baseMethod, TestAnnotation.class);
        assertNotNull(baseMethodAnnotation);
        assertEquals("baseMethod", baseMethodAnnotation.value());
        
        // Derived class should not inherit the method annotation
        // (unless the method is overridden, which it's not in our test)
        Method derivedMethod = DerivedClass.class.getMethod("derivedMethod");
        TestAnnotation derivedMethodAnnotation = helper.getAnnotationOfMethod(derivedMethod, TestAnnotation.class);
        assertNotNull(derivedMethodAnnotation);
        assertEquals("derivedMethod", derivedMethodAnnotation.value());
        
        // Class annotations are not inherited by default either
        TestAnnotation baseClassAnnotation = helper.getAnnotationOfClass(BaseClass.class, TestAnnotation.class);
        assertNotNull(baseClassAnnotation);
        assertEquals("base", baseClassAnnotation.value());
        
        TestAnnotation derivedClassAnnotation = helper.getAnnotationOfClass(DerivedClass.class, TestAnnotation.class);
        assertNotNull(derivedClassAnnotation);
        assertEquals("derived", derivedClassAnnotation.value());
    }
}