package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeelRandomHelperTest extends KeelUnitTest {
    @org.junit.jupiter.api.Test
    void testGenerateRandomString() {
        String randomString = KeelRandomHelper.generateRandomString(10);
        assertNotNull(randomString);
        assertEquals(10, randomString.length());
    }

    @org.junit.jupiter.api.Test
    void testGenerateRandomAlphanumericString() {
        String randomString = KeelRandomHelper.generateRandomAlphanumericString(15);
        assertNotNull(randomString);
        assertEquals(15, randomString.length());
        assertTrue(randomString.matches("[a-zA-Z0-9]+"));
    }

    @org.junit.jupiter.api.Test
    void testGetRandomElement() {
        List<String> list = List.of("apple", "banana", "orange");
        String randomElement = KeelRandomHelper.getRandomElement(list);
        assertNotNull(randomElement);
        assertTrue(list.contains(randomElement));
    }

    @org.junit.jupiter.api.Test
    void testShuffleList() {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        List<Integer> shuffledList = KeelRandomHelper.shuffleList(list);
        assertNotNull(shuffledList);
        assertEquals(list.size(), shuffledList.size());
        assertTrue(shuffledList.containsAll(list));
    }

    @org.junit.jupiter.api.Test
    void testGenerateRandomInt() {
        int min = 1;
        int max = 100;
        int randomInt = KeelRandomHelper.generateRandomInt(min, max);
        assertNotNull(randomInt);
        assertTrue(randomInt >= min && randomInt <= max);
    }

    @org.junit.jupiter.api.Test
    void testGenerateUniqueRandomStrings() {
        int count = 5;
        int length = 8;
        List<String> uniqueRandomStrings = KeelRandomHelper.generateUniqueRandomStrings(count, length);
        assertNotNull(uniqueRandomStrings);
        assertEquals(count, uniqueRandomStrings.size());
        for (String s : uniqueRandomStrings) {
            assertEquals(length, s.length());
        }
        // Check uniqueness
        Map<String, Boolean> map = new java.util.HashMap<>();
        for (String s : uniqueRandomStrings) {
            assertFalse(map.containsKey(s));
            map.put(s, true);
        }
    }
}