package io.github.sinri.keel.core;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ValueBoxTest extends KeelJUnit5Test {

    private ValueBox<String> stringBox;
    private ValueBox<Integer> intBox;

    public ValueBoxTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    public void setUp() {
        stringBox = new ValueBox<>();
        intBox = new ValueBox<>();
    }

    @Test
    @DisplayName("Test default constructor")
    void testDefaultConstructor() {
        ValueBox<String> box = new ValueBox<>();
        assertFalse(box.isValueAlreadySet());
        assertThrows(IllegalStateException.class, box::getValue);
        assertEquals("fallback", box.getValueOrElse("fallback"));
    }

    @Test
    @DisplayName("Test constructor with initial value")
    void testConstructorWithValue() {
        ValueBox<String> box = new ValueBox<>("test");
        assertTrue(box.isValueAlreadySet());
        assertEquals("test", box.getValue());
        assertEquals("test", box.getValueOrElse("fallback"));
    }

    @Test
    @DisplayName("Test constructor with null value")
    void testConstructorWithNullValue() {
        ValueBox<String> box = new ValueBox<>(null);
        assertTrue(box.isValueAlreadySet());
        assertNull(box.getValue());
        assertTrue(box.isValueSetToNull());
        assertFalse(box.isValueSetAndNotNull());
    }

    @Test
    @DisplayName("Test constructor with value and lifetime")
    void testConstructorWithValueAndLifetime() {
        ValueBox<String> box = new ValueBox<>("test", 1000);
        assertTrue(box.isValueAlreadySet());
        assertEquals("test", box.getValue());
    }

    @Test
    @DisplayName("Test setValue without expiration")
    void testSetValueWithoutExpiration() {
        stringBox.setValue("hello");
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());
        assertTrue(stringBox.isValueSetAndNotNull());
        assertFalse(stringBox.isValueSetToNull());
    }

    @Test
    @DisplayName("Test setValue with null")
    void testSetValueWithNull() {
        stringBox.setValue(null);
        assertTrue(stringBox.isValueAlreadySet());
        assertNull(stringBox.getValue());
        assertTrue(stringBox.isValueSetToNull());
        assertFalse(stringBox.isValueSetAndNotNull());
    }

    @Test
    @DisplayName("Test setValue with expiration")
    void testSetValueWithExpiration() {
        stringBox.setValue("hello", 100);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());
    }

    @Test
    @DisplayName("Test setValue with zero lifetime (no expiration)")
    void testSetValueWithZeroLifetime() {
        stringBox.setValue("hello", 0);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());
    }

    @Test
    @DisplayName("Test setValue with negative lifetime (no expiration)")
    void testSetValueWithNegativeLifetime() {
        stringBox.setValue("hello", -100);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());
    }

    @Test
    @DisplayName("Test getValue throws exception when not set")
    void testGetValueThrowsExceptionWhenNotSet() {
        assertThrows(IllegalStateException.class, () -> stringBox.getValue());
    }

    @Test
    @DisplayName("Test getNonNullValue")
    void testGetNonNullValue() {
        stringBox.setValue("hello");
        assertEquals("hello", stringBox.getNonNullValue());
    }

    @Test
    @DisplayName("Test getNonNullValue throws exception when null")
    void testGetNonNullValueThrowsExceptionWhenNull() {
        stringBox.setValue(null);
        assertThrows(NullPointerException.class, () -> stringBox.getNonNullValue());
    }

    @Test
    @DisplayName("Test getNonNullValue throws exception when not set")
    void testGetNonNullValueThrowsExceptionWhenNotSet() {
        assertThrows(IllegalStateException.class, () -> stringBox.getNonNullValue());
    }

    @Test
    @DisplayName("Test getValueOrElse")
    void testGetValueOrElse() {
        // Test with no value set
        assertEquals("fallback", stringBox.getValueOrElse("fallback"));

        // Test with value set
        stringBox.setValue("hello");
        assertEquals("hello", stringBox.getValueOrElse("fallback"));

        // Test with null value set
        stringBox.setValue(null);
        assertNull(stringBox.getValueOrElse("fallback"));
    }

    @Test
    @DisplayName("Test clear method")
    void testClear() {
        stringBox.setValue("hello");
        assertTrue(stringBox.isValueAlreadySet());

        stringBox.clear();
        assertFalse(stringBox.isValueAlreadySet());
        assertThrows(IllegalStateException.class, () -> stringBox.getValue());
        assertEquals("fallback", stringBox.getValueOrElse("fallback"));
    }

    @Test
    @DisplayName("Test method chaining")
    void testMethodChaining() {
        ValueBox<String> result = stringBox.setValue("hello").clear().setValue("world");
        assertSame(stringBox, result);
        assertEquals("world", stringBox.getValue());
    }

    @Test
    @DisplayName("Test expiration mechanism")
    void testExpirationMechanism() throws InterruptedException {
        // Set value with 100ms lifetime
        stringBox.setValue("hello", 100);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());

        // Wait for expiration
        Thread.sleep(150);

        // Value should be expired and cleared
        assertFalse(stringBox.isValueAlreadySet());
        assertThrows(IllegalStateException.class, () -> stringBox.getValue());
        assertEquals("fallback", stringBox.getValueOrElse("fallback"));
    }

    @Test
    @DisplayName("Test expiration with zero lifetime")
    void testExpirationWithZeroLifetime() throws InterruptedException {
        stringBox.setValue("hello", 0);
        assertTrue(stringBox.isValueAlreadySet());

        // Wait some time
        Thread.sleep(100);

        // Value should still be valid (no expiration)
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("hello", stringBox.getValue());
    }

    @Test
    @DisplayName("Test isValueSetToNull")
    void testIsValueSetToNull() {
        // Not set
        assertFalse(stringBox.isValueSetToNull());

        // Set to null
        stringBox.setValue(null);
        assertTrue(stringBox.isValueSetToNull());

        // Set to non-null
        stringBox.setValue("hello");
        assertFalse(stringBox.isValueSetToNull());
    }

    @Test
    @DisplayName("Test isValueSetAndNotNull")
    void testIsValueSetAndNotNull() {
        // Not set
        assertFalse(stringBox.isValueSetAndNotNull());

        // Set to null
        stringBox.setValue(null);
        assertFalse(stringBox.isValueSetAndNotNull());

        // Set to non-null
        stringBox.setValue("hello");
        assertTrue(stringBox.isValueSetAndNotNull());
    }

    @Test
    @DisplayName("Test thread safety")
    void testThreadSafety() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        AtomicInteger successCount = new AtomicInteger(0);

        // Create 100 threads that set and get values
        for (int i = 0; i < 100; i++) {
            final int value = i;
            executor.submit(() -> {
                try {
                    intBox.setValue(value);
                    Integer retrieved = intBox.getValue();
                    if (retrieved != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Expected in concurrent environment
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Should have some successful operations
        assertTrue(successCount.get() > 0);
        // Box should be in a valid state
        assertTrue(intBox.isValueAlreadySet());
        assertNotNull(intBox.getValue());
    }

    @Test
    @DisplayName("Test concurrent expiration")
    void testConcurrentExpiration() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger setCount = new AtomicInteger(0);
        AtomicInteger getCount = new AtomicInteger(0);

        // Set value with short lifetime
        stringBox.setValue("test", 50);

        // Start 5 threads that try to get the value
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        try {
                            stringBox.getValue();
                            getCount.incrementAndGet();
                        } catch (IllegalStateException e) {
                            // Expected when expired
                        }

                        // Reset value sometimes
                        if (j % 3 == 0) {
                            stringBox.setValue("test" + j, 50);
                            setCount.incrementAndGet();
                        }

                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Should have some operations
        assertTrue(setCount.get() > 0);
        assertTrue(getCount.get() > 0);
    }

    @Test
    @DisplayName("Test expiration edge cases")
    void testExpirationEdgeCases() throws InterruptedException {
        // Test very short lifetime
        stringBox.setValue("test", 1);
        Thread.sleep(5);
        assertFalse(stringBox.isValueAlreadySet());

        // Test setting expired value
        stringBox.setValue("test", -1);
        assertTrue(stringBox.isValueAlreadySet()); // Negative lifetime means no expiration

        // Test clearing expired value
        stringBox.setValue("test", 1);
        Thread.sleep(5);
        stringBox.clear(); // Should not throw
        assertFalse(stringBox.isValueAlreadySet());
    }

    @Test
    @DisplayName("Test type safety")
    void testTypeSafety() {
        ValueBox<Integer> integerBox = new ValueBox<>();
        ValueBox<String> stringBox = new ValueBox<>();

        integerBox.setValue(42);
        stringBox.setValue("hello");

        assertEquals(Integer.valueOf(42), integerBox.getValue());
        assertEquals("hello", stringBox.getValue());

        // Test with custom objects
        ValueBox<Object> objectBox = new ValueBox<>();
        Object testObject = new Object();
        objectBox.setValue(testObject);
        assertSame(testObject, objectBox.getValue());
    }

    @Test
    @DisplayName("Test multiple operations sequence")
    void testMultipleOperationsSequence() {
        // Initial state
        assertFalse(stringBox.isValueAlreadySet());

        // Set value
        stringBox.setValue("first");
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("first", stringBox.getValue());

        // Update value
        stringBox.setValue("second");
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("second", stringBox.getValue());

        // Clear
        stringBox.clear();
        assertFalse(stringBox.isValueAlreadySet());

        // Set with expiration
        stringBox.setValue("third", 1000);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("third", stringBox.getValue());

        // Update expiration
        stringBox.setValue("fourth", 2000);
        assertTrue(stringBox.isValueAlreadySet());
        assertEquals("fourth", stringBox.getValue());
    }
}