package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelDigestHelperTest extends KeelJUnit5Test {

    public KeelDigestHelperTest(Vertx vertx) {
        super(vertx);
    }

    private KeelDigestHelper getHelper() {
        return Keel.digestHelper();
    }

    // Test MD5 digest methods
    @Test
    @DisplayName("MD5摘要 - 字符串输入小写")
    void testMd5_StringInputLowercase() {
        String input = "Hello World";
        String result = getHelper().md5(input);
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("MD5摘要 - 字符串输入大写")
    void testMD5_StringInputUppercase() {
        String input = "Hello World";
        String result = getHelper().MD5(input);
        assertEquals("B10A8DB164E0754105B7A99BE72E3FE5", result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9A-F]+"));
    }

    @Test
    @DisplayName("MD5摘要 - 字节数组输入小写")
    void testMd5_ByteArrayInputLowercase() {
        byte[] input = "Hello World".getBytes();
        String result = getHelper().md5(input);
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", result);
    }

    @Test
    @DisplayName("MD5摘要 - 字节数组输入大写")
    void testMD5_ByteArrayInputUppercase() {
        byte[] input = "Hello World".getBytes();
        String result = getHelper().MD5(input);
        assertEquals("B10A8DB164E0754105B7A99BE72E3FE5", result);
    }

    @Test
    @DisplayName("MD5摘要 - 空字符串")
    void testMd5_EmptyString() {
        String result = getHelper().md5("");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    @DisplayName("MD5摘要 - 中文字符")
    void testMd5_ChineseCharacters() {
        String input = "你好世界";
        String result = getHelper().md5(input);
        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("MD5摘要 - 特殊字符")
    void testMd5_SpecialCharacters() {
        String input = "!@#$%^&*()_+{}|:<>?[]\";',./<>?";
        String result = getHelper().md5(input);
        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("MD5摘要 - 一致性测试")
    void testMd5_Consistency() {
        String input = "Test consistency";
        String result1 = getHelper().md5(input);
        String result2 = getHelper().md5(input);
        assertEquals(result1, result2);
    }

    @Test
    @DisplayName("MD5摘要 - 大小写转换测试")
    void testMd5_CaseConversion() {
        String input = "Test Case";
        String lowercase = getHelper().md5(input);
        String uppercase = getHelper().MD5(input);
        assertEquals(lowercase.toUpperCase(), uppercase);
        assertEquals(uppercase.toLowerCase(), lowercase);
    }

    // Test SHA-1 digest methods
    @Test
    @DisplayName("SHA1摘要 - 字符串输入小写")
    void testSha1_StringInputLowercase() {
        String input = "Hello World";
        String result = getHelper().sha1(input);
        assertEquals("0a4d55a8d778e5022fab701977c5d840bbc486d0", result);
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("SHA1摘要 - 字符串输入大写")
    void testSHA1_StringInputUppercase() {
        String input = "Hello World";
        String result = getHelper().SHA1(input);
        assertEquals("0A4D55A8D778E5022FAB701977C5D840BBC486D0", result);
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9A-F]+"));
    }

    @Test
    @DisplayName("SHA1摘要 - 空字符串")
    void testSha1_EmptyString() {
        String result = getHelper().sha1("");
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", result);
    }

    // Test SHA-512 digest methods
    @Test
    @DisplayName("SHA512摘要 - 字符串输入小写")
    void testSha512_StringInputLowercase() {
        String input = "Hello World";
        String result = getHelper().sha512(input);
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("SHA512摘要 - 字符串输入大写")
    void testSHA512_StringInputUppercase() {
        String input = "Hello World";
        String result = getHelper().SHA512(input);
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9A-F]+"));
    }

    @Test
    @DisplayName("SHA512摘要 - 空字符串")
    void testSha512_EmptyString() {
        String result = getHelper().sha512("");
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    // Test generic digest methods
    @Test
    @DisplayName("通用摘要算法 - MD5小写")
    void testDigestToLower_MD5() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToLower(KeelDigestHelper.DIGEST_ALGO_MD5, input);
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", result);
    }

    @Test
    @DisplayName("通用摘要算法 - MD5大写")
    void testDigestToUpper_MD5() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToUpper(KeelDigestHelper.DIGEST_ALGO_MD5, input);
        assertEquals("B10A8DB164E0754105B7A99BE72E3FE5", result);
    }

    @Test
    @DisplayName("通用摘要算法 - SHA1小写")
    void testDigestToLower_SHA1() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToLower(KeelDigestHelper.DIGEST_ALGO_SHA_1, input);
        assertEquals("0a4d55a8d778e5022fab701977c5d840bbc486d0", result);
    }

    @Test
    @DisplayName("通用摘要算法 - SHA1大写")
    void testDigestToUpper_SHA1() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToUpper(KeelDigestHelper.DIGEST_ALGO_SHA_1, input);
        assertEquals("0A4D55A8D778E5022FAB701977C5D840BBC486D0", result);
    }

    @Test
    @DisplayName("通用摘要算法 - SHA512小写")
    void testDigestToLower_SHA512() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToLower(KeelDigestHelper.DIGEST_ALGO_SHA_512, input);
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("通用摘要算法 - SHA512大写")
    void testDigestToUpper_SHA512() throws NoSuchAlgorithmException {
        String input = "Hello World";
        String result = getHelper().digestToUpper(KeelDigestHelper.DIGEST_ALGO_SHA_512, input);
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9A-F]+"));
    }

    @Test
    @DisplayName("通用摘要算法 - 字节数组输入")
    void testDigestToLower_ByteArray() throws NoSuchAlgorithmException {
        byte[] input = "Hello World".getBytes();
        String result = getHelper().digestToLower(KeelDigestHelper.DIGEST_ALGO_MD5, input);
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", result);
    }

    @Test
    @DisplayName("通用摘要算法 - 无效算法异常")
    void testDigest_InvalidAlgorithm() {
        assertThrows(NoSuchAlgorithmException.class, () -> {
            getHelper().digestToLower("INVALID_ALGORITHM", "test");
        });
    }

    // Test HMAC methods
    @Test
    @DisplayName("HMAC-SHA1 - Base64输出")
    void testHmacSha1_Base64Output() {
        String data = "Hello World";
        String key = "secret";
        String result = getHelper().hmac_sha1_base64(data, key);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        // Base64字符集测试
        assertTrue(result.matches("[A-Za-z0-9+/=]+"));
    }

    @Test
    @DisplayName("HMAC-SHA1 - 十六进制输出小写")
    void testHmacSha1_HexOutputLowercase() {
        String data = "Hello World";
        String key = "secret";
        String result = getHelper().hmac_sha1_hex(data, key);
        assertNotNull(result);
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("HMAC-SHA1 - 十六进制输出大写")
    void testHmacSha1_HexOutputUppercase() {
        String data = "Hello World";
        String key = "secret";
        String result = getHelper().HMAC_SHA1_HEX(data, key);
        assertNotNull(result);
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9A-F]+"));
    }

    @Test
    @DisplayName("HMAC-SHA1 - 空数据")
    void testHmacSha1_EmptyData() {
        String result = getHelper().hmac_sha1_base64("", "secret");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    @DisplayName("HMAC-SHA1 - 空密钥")
    void testHmacSha1_EmptyKey() {
        assertThrows(RuntimeException.class, () -> {
            getHelper().hmac_sha1_base64("Hello World", "");
        });
    }

    @Test
    @DisplayName("HMAC-SHA1 - 不同密钥产生不同结果")
    void testHmacSha1_DifferentKeys() {
        String data = "Hello World";
        String result1 = getHelper().hmac_sha1_base64(data, "key1");
        String result2 = getHelper().hmac_sha1_base64(data, "key2");
        assertNotEquals(result1, result2);
    }

    @Test
    @DisplayName("HMAC-SHA1 - 相同输入产生相同结果")
    void testHmacSha1_SameInputSameOutput() {
        String data = "Hello World";
        String key = "secret";
        String result1 = getHelper().hmac_sha1_base64(data, key);
        String result2 = getHelper().hmac_sha1_base64(data, key);
        assertEquals(result1, result2);
    }

    @Test
    @DisplayName("HMAC-SHA1 - 大小写输出对比")
    void testHmacSha1_CaseComparison() {
        String data = "Hello World";
        String key = "secret";
        String lowercase = getHelper().hmac_sha1_hex(data, key);
        String uppercase = getHelper().HMAC_SHA1_HEX(data, key);
        assertEquals(lowercase.toUpperCase(), uppercase);
        assertEquals(uppercase.toLowerCase(), lowercase);
    }

    // Test MD5 MessageDigest static method
    @Test
    @DisplayName("获取MD5 MessageDigest实例")
    void testGetMD5MessageDigest() {
        MessageDigest md5 = KeelDigestHelper.getMD5MessageDigest();
        assertNotNull(md5);
        assertEquals("MD5", md5.getAlgorithm());
    }

    @Test
    @DisplayName("MD5 MessageDigest - 多次获取同一实例")
    void testGetMD5MessageDigest_ThreadLocal() {
        MessageDigest md1 = KeelDigestHelper.getMD5MessageDigest();
        MessageDigest md2 = KeelDigestHelper.getMD5MessageDigest();
        assertSame(md1, md2); // FastThreadLocal应该返回同一实例
    }

    // Test performance and edge cases
    @Test
    @DisplayName("摘要算法 - 性能测试")
    void testDigestPerformance() {
        String largeInput = "A".repeat(10000);
        
        // 测试各种算法的性能
        long start = System.currentTimeMillis();
        String md5Result = getHelper().md5(largeInput);
        long md5Time = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        String sha1Result = getHelper().sha1(largeInput);
        long sha1Time = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        String sha512Result = getHelper().sha512(largeInput);
        long sha512Time = System.currentTimeMillis() - start;
        
        // 验证结果
        assertEquals(32, md5Result.length());
        assertEquals(40, sha1Result.length());
        assertEquals(128, sha512Result.length());
        
        getUnitTestLogger().info("MD5 time: " + md5Time + "ms");
        getUnitTestLogger().info("SHA1 time: " + sha1Time + "ms");
        getUnitTestLogger().info("SHA512 time: " + sha512Time + "ms");
    }

    @Test
    @DisplayName("摘要算法 - 随机数据测试")
    void testDigestRandomData() {
        SecureRandom random = new SecureRandom();
        byte[] randomData = new byte[1000];
        random.nextBytes(randomData);
        
        String md5Result = getHelper().md5(randomData);
        String sha1Result = getHelper().sha1(new String(randomData));
        String sha512Result = getHelper().sha512(new String(randomData));
        
        assertEquals(32, md5Result.length());
        assertEquals(40, sha1Result.length());
        assertEquals(128, sha512Result.length());
        
        assertTrue(md5Result.matches("[0-9a-f]+"));
        assertTrue(sha1Result.matches("[0-9a-f]+"));
        assertTrue(sha512Result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("摘要算法 - 雪崩效应测试")
    void testDigestAvalancheEffect() {
        String input1 = "Hello World";
        String input2 = "Hello World!"; // 仅一个字符不同
        
        String md5_1 = getHelper().md5(input1);
        String md5_2 = getHelper().md5(input2);
        
        String sha1_1 = getHelper().sha1(input1);
        String sha1_2 = getHelper().sha1(input2);
        
        // 验证微小变化导致结果完全不同
        assertNotEquals(md5_1, md5_2);
        assertNotEquals(sha1_1, sha1_2);
        
        // 计算汉明距离（不同字符数）
        int md5HammingDistance = calculateHammingDistance(md5_1, md5_2);
        int sha1HammingDistance = calculateHammingDistance(sha1_1, sha1_2);
        
        // 雪崩效应应该导致约50%的位发生变化
        assertTrue(md5HammingDistance > 10, "MD5 Hamming distance: " + md5HammingDistance);
        assertTrue(sha1HammingDistance > 10, "SHA1 Hamming distance: " + sha1HammingDistance);
    }

    private int calculateHammingDistance(String s1, String s2) {
        if (s1.length() != s2.length()) {
            throw new IllegalArgumentException("Strings must have the same length");
        }
        
        int distance = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }

    @Test
    @DisplayName("摘要算法 - 边界值测试")
    void testDigestBoundaryValues() {
        // 测试各种边界情况
        String[] testInputs = {
            "",
            "a",
            "ab",
            "abc",
            "a".repeat(55), // MD5块大小边界
            "a".repeat(56),
            "a".repeat(64), // SHA1块大小边界
            "a".repeat(128), // SHA512块大小边界
            "a".repeat(1000)
        };
        
        for (String input : testInputs) {
            String md5Result = getHelper().md5(input);
            String sha1Result = getHelper().sha1(input);
            String sha512Result = getHelper().sha512(input);
            
            assertEquals(32, md5Result.length(), "MD5 failed for input length: " + input.length());
            assertEquals(40, sha1Result.length(), "SHA1 failed for input length: " + input.length());
            assertEquals(128, sha512Result.length(), "SHA512 failed for input length: " + input.length());
        }
    }
} 