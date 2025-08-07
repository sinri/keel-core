package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeelRandomHelperTest extends KeelUnitTest {
    private KeelRandomHelper randomHelper;

    @BeforeEach
    public void setUp() {
        randomHelper = KeelRandomHelper.getInstance();
    }

    @Test
    void testSingletonPattern() {
        KeelRandomHelper instance1 = KeelRandomHelper.getInstance();
        KeelRandomHelper instance2 = KeelRandomHelper.getInstance();
        assertSame(instance1, instance2, "Singleton instances should be the same");
    }

    @Test
    void testGenerateRandomString() {
        // Test normal case
        String randomString = randomHelper.generateRandomString(10);
        assertNotNull(randomString);
        assertEquals(10, randomString.length());
        
        // Test with different lengths
        String shortString = randomHelper.generateRandomString(1);
        assertEquals(1, shortString.length());
        
        String longString = randomHelper.generateRandomString(100);
        assertEquals(100, longString.length());
        
        // Test that generated strings are different
        String anotherString = randomHelper.generateRandomString(10);
        assertNotEquals(randomString, anotherString, "Random strings should be different");
    }

    @Test
    void testGenerateRandomStringWithInvalidLength() {
        // Test zero length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateRandomString(0);
        }, "Should throw exception for zero length");
        
        // Test negative length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateRandomString(-1);
        }, "Should throw exception for negative length");
    }

    @Test
    void testGenerateRandomAlphanumericString() {
        // Test normal case
        String randomString = randomHelper.generateRandomAlphanumericString(15);
        assertNotNull(randomString);
        assertEquals(15, randomString.length());
        assertTrue(randomString.matches("[a-zA-Z0-9]+"), "Should only contain alphanumeric characters");
        
        // Test with different lengths
        String shortString = randomHelper.generateRandomAlphanumericString(1);
        assertEquals(1, shortString.length());
        assertTrue(shortString.matches("[a-zA-Z0-9]"));
        
        String longString = randomHelper.generateRandomAlphanumericString(50);
        assertEquals(50, longString.length());
        assertTrue(longString.matches("[a-zA-Z0-9]+"));
        
        // Test that generated strings are different
        String anotherString = randomHelper.generateRandomAlphanumericString(15);
        assertNotEquals(randomString, anotherString, "Random strings should be different");
    }

    @Test
    void testGenerateRandomAlphanumericStringWithInvalidLength() {
        // Test zero length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateRandomAlphanumericString(0);
        }, "Should throw exception for zero length");
        
        // Test negative length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateRandomAlphanumericString(-1);
        }, "Should throw exception for negative length");
    }

    @Test
    void testGetRandomElement() {
        List<String> list = List.of("apple", "banana", "orange", "grape", "kiwi");
        
        // Test multiple calls to ensure randomness
        Set<String> selectedElements = Set.of();
        for (int i = 0; i < 100; i++) {
            String randomElement = randomHelper.getRandomElement(list);
            assertNotNull(randomElement);
            assertTrue(list.contains(randomElement), "Selected element should be in the original list");
        }
        
        // Test with single element list
        List<String> singleElementList = List.of("single");
        String element = randomHelper.getRandomElement(singleElementList);
        assertEquals("single", element);
    }

    @Test
    void testGetRandomElementWithInvalidInput() {
        // Test null list
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.getRandomElement(null);
        }, "Should throw exception for null list");
        
        // Test empty list
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.getRandomElement(List.of());
        }, "Should throw exception for empty list");
    }

    @Test
    void testShuffleList() {
        List<Integer> originalList = List.of(1, 2, 3, 4, 5);
        
        // Test normal case
        List<Integer> shuffledList = randomHelper.shuffleList(originalList);
        assertNotNull(shuffledList);
        assertEquals(originalList.size(), shuffledList.size());
        assertTrue(shuffledList.containsAll(originalList));
        assertTrue(originalList.containsAll(shuffledList));
        
        // Test that shuffling multiple times produces different results
        List<Integer> shuffledList2 = randomHelper.shuffleList(originalList);
        assertNotEquals(shuffledList, shuffledList2, "Shuffled lists should be different");
        
        // Test with single element list
        List<String> singleElementList = List.of("single");
        List<String> shuffledSingle = randomHelper.shuffleList(singleElementList);
        assertEquals(singleElementList, shuffledSingle);
        
        // Test with empty list
        List<String> emptyList = List.of();
        List<String> shuffledEmpty = randomHelper.shuffleList(emptyList);
        assertTrue(shuffledEmpty.isEmpty());
    }

    @Test
    void testShuffleListWithNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.shuffleList(null);
        }, "Should throw exception for null list");
    }

    @Test
    void testGenerateRandomInt() {
        int min = 1;
        int max = 100;
        
        // Test normal case
        int randomInt = randomHelper.generateRandomInt(min, max);
        assertTrue(randomInt >= min && randomInt <= max, "Random int should be within range");
        
        // Test boundary values
        int minValue = randomHelper.generateRandomInt(min, min);
        assertEquals(min, minValue);
        
        int maxValue = randomHelper.generateRandomInt(max, max);
        assertEquals(max, maxValue);
        
        // Test with negative range
        int negativeRandom = randomHelper.generateRandomInt(-10, -1);
        assertTrue(negativeRandom >= -10 && negativeRandom <= -1);
        
        // Test multiple calls to ensure randomness
        Set<Integer> generatedValues = Set.of();
        for (int i = 0; i < 100; i++) {
            int value = randomHelper.generateRandomInt(min, max);
            assertTrue(value >= min && value <= max);
        }
    }

    @Test
    void testGenerateRandomIntWithInvalidRange() {
        // Test when min > max
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateRandomInt(10, 5);
        }, "Should throw exception when min > max");
    }

    @Test
    void testGenerateUniqueRandomStrings() {
        int count = 5;
        int length = 8;
        
        // Test normal case
        List<String> uniqueRandomStrings = randomHelper.generateUniqueRandomStrings(count, length);
        assertNotNull(uniqueRandomStrings);
        assertEquals(count, uniqueRandomStrings.size());
        
        // Check each string has correct length
        for (String s : uniqueRandomStrings) {
            assertEquals(length, s.length());
        }
        
        // Check uniqueness
        Set<String> uniqueSet = new HashSet<>();
        for (String s : uniqueRandomStrings) {
            assertFalse(uniqueSet.contains(s), "All strings should be unique");
            uniqueSet.add(s);
        }
        
        // Test with single string
        List<String> singleString = randomHelper.generateUniqueRandomStrings(1, 10);
        assertEquals(1, singleString.size());
        assertEquals(10, singleString.get(0).length());
    }

    @Test
    void testGenerateUniqueRandomStringsWithInvalidParameters() {
        // Test zero count
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateUniqueRandomStrings(0, 10);
        }, "Should throw exception for zero count");
        
        // Test negative count
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateUniqueRandomStrings(-1, 10);
        }, "Should throw exception for negative count");
        
        // Test zero length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateUniqueRandomStrings(5, 0);
        }, "Should throw exception for zero length");
        
        // Test negative length
        assertThrows(IllegalArgumentException.class, () -> {
            randomHelper.generateUniqueRandomStrings(5, -1);
        }, "Should throw exception for negative length");
    }

    @Test
    void testGetPRNG() {
        // Test that PRNG is not null
        VertxContextPRNG prng = randomHelper.getPRNG();
        assertNotNull(prng, "PRNG should not be null");
        
        // Test that multiple calls return the same instance (singleton behavior)
        VertxContextPRNG prng2 = randomHelper.getPRNG();
        assertSame(prng, prng2, "PRNG instances should be the same");
        
        // Test that PRNG can generate random numbers
        int randomNumber = prng.nextInt(100);
        assertTrue(randomNumber >= 0 && randomNumber < 100);
    }

    @Test
    void testRandomnessDistribution() {
        // Test that random strings have good distribution
        int length = 1000;
        String randomString = randomHelper.generateRandomString(length);
        
        // Count character types
        int uppercase = 0, lowercase = 0, digits = 0, special = 0;
        for (char c : randomString.toCharArray()) {
            if (Character.isUpperCase(c)) uppercase++;
            else if (Character.isLowerCase(c)) lowercase++;
            else if (Character.isDigit(c)) digits++;
            else special++;
        }
        
        // Basic distribution check (not too strict, just ensure all types are present)
        assertTrue(uppercase > 0, "Should contain uppercase letters");
        assertTrue(lowercase > 0, "Should contain lowercase letters");
        assertTrue(digits > 0, "Should contain digits");
        assertTrue(special > 0, "Should contain special characters");
    }

    @Test
    void testAlphanumericDistribution() {
        // Test that alphanumeric strings have good distribution
        int length = 1000;
        String randomString = randomHelper.generateRandomAlphanumericString(length);
        
        // Count character types
        int uppercase = 0, lowercase = 0, digits = 0;
        for (char c : randomString.toCharArray()) {
            if (Character.isUpperCase(c)) uppercase++;
            else if (Character.isLowerCase(c)) lowercase++;
            else if (Character.isDigit(c)) digits++;
        }
        
        // Basic distribution check
        assertTrue(uppercase > 0, "Should contain uppercase letters");
        assertTrue(lowercase > 0, "Should contain lowercase letters");
        assertTrue(digits > 0, "Should contain digits");
        
        // Verify no special characters
        assertTrue(randomString.matches("[a-zA-Z0-9]+"), "Should only contain alphanumeric characters");
    }
}