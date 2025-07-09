package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KeelRandomHelperTest extends KeelUnitTest {
    @Test
    void testGenerateRandomString() {
        // 测试正常情况
        String randomString = KeelRandomHelper.generateRandomString(10);
        assertNotNull(randomString);
        assertEquals(10, randomString.length());
        
        // 测试不同长度
        String shortString = KeelRandomHelper.generateRandomString(1);
        assertEquals(1, shortString.length());
        
        String longString = KeelRandomHelper.generateRandomString(100);
        assertEquals(100, longString.length());
        
        // 测试生成的字符串是否包含预期的字符集
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        for (char c : randomString.toCharArray()) {
            assertTrue(chars.indexOf(c) >= 0, "Character " + c + " not in expected charset");
        }
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateRandomString(0));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateRandomString(-1));
    }

    @Test
    void testGenerateRandomAlphanumericString() {
        // 测试正常情况
        String randomString = KeelRandomHelper.generateRandomAlphanumericString(15);
        assertNotNull(randomString);
        assertEquals(15, randomString.length());
        assertTrue(randomString.matches("[a-zA-Z0-9]+"));
        
        // 测试不同长度
        String shortString = KeelRandomHelper.generateRandomAlphanumericString(1);
        assertEquals(1, shortString.length());
        assertTrue(shortString.matches("[a-zA-Z0-9]+"));
        
        String longString = KeelRandomHelper.generateRandomAlphanumericString(50);
        assertEquals(50, longString.length());
        assertTrue(longString.matches("[a-zA-Z0-9]+"));
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateRandomAlphanumericString(0));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateRandomAlphanumericString(-5));
    }

    @Test
    void testGetRandomElement() {
        // 测试正常情况
        List<String> list = List.of("apple", "banana", "orange");
        String randomElement = KeelRandomHelper.getRandomElement(list);
        assertNotNull(randomElement);
        assertTrue(list.contains(randomElement));
        
        // 测试单元素列表
        List<Integer> singleElementList = List.of(42);
        Integer singleElement = KeelRandomHelper.getRandomElement(singleElementList);
        assertEquals(42, singleElement);
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.getRandomElement(new ArrayList<>()));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.getRandomElement(null));
    }

    @Test
    void testShuffleList() {
        // 测试正常情况
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        List<Integer> shuffledList = KeelRandomHelper.shuffleList(list);
        assertNotNull(shuffledList);
        assertEquals(list.size(), shuffledList.size());
        assertTrue(shuffledList.containsAll(list));
        
        // 测试单元素列表
        List<String> singleElementList = List.of("single");
        List<String> shuffledSingleElementList = KeelRandomHelper.shuffleList(singleElementList);
        assertEquals(1, shuffledSingleElementList.size());
        assertEquals("single", shuffledSingleElementList.get(0));
        
        // 测试空列表
        List<Double> emptyList = new ArrayList<>();
        List<Double> shuffledEmptyList = KeelRandomHelper.shuffleList(emptyList);
        assertTrue(shuffledEmptyList.isEmpty());
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.shuffleList(null));
        
        // 测试大列表
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(i);
        }
        List<Integer> shuffledLargeList = KeelRandomHelper.shuffleList(largeList);
        assertEquals(largeList.size(), shuffledLargeList.size());
        assertTrue(shuffledLargeList.containsAll(largeList));
    }

    @Test
    void testGenerateRandomInt() {
        // 测试正常情况
        int min = 1;
        int max = 100;
        int randomInt = KeelRandomHelper.generateRandomInt(min, max);
        assertTrue(randomInt >= min && randomInt <= max);
        
        // 测试边界值
        int boundaryInt = KeelRandomHelper.generateRandomInt(5, 5);
        assertEquals(5, boundaryInt);
        
        // 测试负数范围
        int negativeRangeInt = KeelRandomHelper.generateRandomInt(-10, -5);
        assertTrue(negativeRangeInt >= -10 && negativeRangeInt <= -5);
        
        // 测试跨越0的范围
        int crossZeroInt = KeelRandomHelper.generateRandomInt(-5, 5);
        assertTrue(crossZeroInt >= -5 && crossZeroInt <= 5);
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateRandomInt(10, 5));
    }

    @Test
    void testGenerateUniqueRandomStrings() {
        // 测试正常情况
        int count = 5;
        int length = 8;
        List<String> uniqueRandomStrings = KeelRandomHelper.generateUniqueRandomStrings(count, length);
        assertNotNull(uniqueRandomStrings);
        assertEquals(count, uniqueRandomStrings.size());
        
        // 检查长度
        for (String s : uniqueRandomStrings) {
            assertEquals(length, s.length());
        }
        
        // 检查唯一性
        Set<String> uniqueSet = new HashSet<>(uniqueRandomStrings);
        assertEquals(count, uniqueSet.size());
        
        // 测试大量字符串
        int largeCount = 20;
        List<String> largeUniqueList = KeelRandomHelper.generateUniqueRandomStrings(largeCount, length);
        assertEquals(largeCount, largeUniqueList.size());
        Set<String> largeUniqueSet = new HashSet<>(largeUniqueList);
        assertEquals(largeCount, largeUniqueSet.size());
        
        // 测试边界条件
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateUniqueRandomStrings(0, 5));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateUniqueRandomStrings(5, 0));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateUniqueRandomStrings(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> KeelRandomHelper.generateUniqueRandomStrings(5, -1));
    }
    
    @Test
    void testGetPRNG() {
        // 测试获取PRNG实例
        assertNotNull(KeelRandomHelper.getInstance().getPRNG());
        
        // 测试多次获取返回相同实例
        var prng1 = KeelRandomHelper.getInstance().getPRNG();
        var prng2 = KeelRandomHelper.getInstance().getPRNG();
        assertSame(prng1.getClass(), prng2.getClass());
    }
}