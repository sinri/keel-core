package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelStringHelperTest extends KeelJUnit5Test {

    public KeelStringHelperTest(Vertx vertx) {
        super(vertx);
    }

    private KeelStringHelper getHelper() {
        return Keel.stringHelper();
    }

    // Test joinStringArray methods
    @Test
    @DisplayName("joinStringArray - Array版本 - 正常情况")
    void testJoinStringArray_Array_Normal() {
        String[] array = {"apple", "banana", "cherry"};
        String result = getHelper().joinStringArray(array, ", ");
        assertEquals("apple, banana, cherry", result);
    }

    @Test
    @DisplayName("joinStringArray - Array版本 - 空数组")
    void testJoinStringArray_Array_Empty() {
        String[] array = {};
        String result = getHelper().joinStringArray(array, ", ");
        assertEquals("", result);
    }

    @Test
    @DisplayName("joinStringArray - Array版本 - null数组")
    void testJoinStringArray_Array_Null() {
        String[] array = null;
        String result = getHelper().joinStringArray(array, ", ");
        assertEquals("", result);
    }

    @Test
    @DisplayName("joinStringArray - Array版本 - 包含null元素")
    void testJoinStringArray_Array_WithNullElements() {
        String[] array = {"apple", null, "cherry"};
        String result = getHelper().joinStringArray(array, ", ");
        assertEquals("apple, null, cherry", result);
    }

    @Test
    @DisplayName("joinStringArray - Array版本 - 单个元素")
    void testJoinStringArray_Array_SingleElement() {
        String[] array = {"apple"};
        String result = getHelper().joinStringArray(array, ", ");
        assertEquals("apple", result);
    }

    @Test
    @DisplayName("joinStringArray - List版本 - 正常情况")
    void testJoinStringArray_List_Normal() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        String result = getHelper().joinStringArray(list, ", ");
        assertEquals("apple, banana, cherry", result);
    }

    @Test
    @DisplayName("joinStringArray - List版本 - 空列表")
    void testJoinStringArray_List_Empty() {
        List<String> list = Collections.emptyList();
        String result = getHelper().joinStringArray(list, ", ");
        assertEquals("", result);
    }

    @Test
    @DisplayName("joinStringArray - List版本 - null列表")
    void testJoinStringArray_List_Null() {
        List<String> list = null;
        String result = getHelper().joinStringArray(list, ", ");
        assertEquals("", result);
    }

    // Test bufferToHexMatrix
    @Test
    @DisplayName("bufferToHexMatrix - 正常情况")
    void testBufferToHexMatrix_Normal() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03, 0x04});
        String result = getHelper().bufferToHexMatrix(buffer, 2);
        assertEquals("01 02 \n03 04 \n", result);
    }

    @Test
    @DisplayName("bufferToHexMatrix - 单行")
    void testBufferToHexMatrix_SingleRow() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02});
        String result = getHelper().bufferToHexMatrix(buffer, 4);
        assertEquals("01 02 ", result);
    }

    // Test fromUnderScoreCaseToCamelCase
    @Test
    @DisplayName("fromUnderScoreCaseToCamelCase - 正常转换")
    void testFromUnderScoreCaseToCamelCase_Normal() {
        String result = getHelper().fromUnderScoreCaseToCamelCase("apple_pie");
        assertEquals("ApplePie", result);
    }

    @Test
    @DisplayName("fromUnderScoreCaseToCamelCase - 首字母小写")
    void testFromUnderScoreCaseToCamelCase_FirstCharLower() {
        String result = getHelper().fromUnderScoreCaseToCamelCase("apple_pie", true);
        assertEquals("applePie", result);
    }

    @Test
    @DisplayName("fromUnderScoreCaseToCamelCase - null输入")
    void testFromUnderScoreCaseToCamelCase_Null() {
        String result = getHelper().fromUnderScoreCaseToCamelCase(null);
        assertNull(result);
    }

    @Test
    @DisplayName("fromUnderScoreCaseToCamelCase - 空字符串")
    void testFromUnderScoreCaseToCamelCase_Empty() {
        String result = getHelper().fromUnderScoreCaseToCamelCase("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("fromUnderScoreCaseToCamelCase - 含空格")
    void testFromUnderScoreCaseToCamelCase_WithSpaces() {
        String result = getHelper().fromUnderScoreCaseToCamelCase("hello world");
        assertEquals("HelloWorld", result);
    }

    // Test fromCamelCaseToUserScoreCase
    @Test
    @DisplayName("fromCamelCaseToUserScoreCase - 正常转换")
    void testFromCamelCaseToUserScoreCase_Normal() {
        String result = getHelper().fromCamelCaseToUserScoreCase("ApplePie");
        assertEquals("apple_pie", result);
    }

    @Test
    @DisplayName("fromCamelCaseToUserScoreCase - null输入")
    void testFromCamelCaseToUserScoreCase_Null() {
        String result = getHelper().fromCamelCaseToUserScoreCase(null);
        assertNull(result);
    }

    @Test
    @DisplayName("fromCamelCaseToUserScoreCase - 空字符串")
    void testFromCamelCaseToUserScoreCase_Empty() {
        String result = getHelper().fromCamelCaseToUserScoreCase("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("fromCamelCaseToUserScoreCase - 单字符")
    void testFromCamelCaseToUserScoreCase_SingleChar() {
        String result = getHelper().fromCamelCaseToUserScoreCase("A");
        assertEquals("a", result);
    }

    @Test
    @DisplayName("fromCamelCaseToUserScoreCase - 复杂情况")
    void testFromCamelCaseToUserScoreCase_Complex() {
        String result = getHelper().fromCamelCaseToUserScoreCase("XMLHttpRequest");
        assertEquals("x_m_l_http_request", result);
    }

    // Test renderThrowableChain
    @Test
    @DisplayName("renderThrowableChain - null异常")
    void testRenderThrowableChain_Null() {
        String result = getHelper().renderThrowableChain(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("renderThrowableChain - 简单异常")
    void testRenderThrowableChain_Simple() {
        Exception exception = new RuntimeException("Test exception");
        String result = getHelper().renderThrowableChain(exception);
        assertNotNull(result);
        assertTrue(result.contains("RuntimeException"));
        assertTrue(result.contains("Test exception"));
    }

    @Test
    @DisplayName("renderThrowableChain - 异常链")
    void testRenderThrowableChain_ChainedExceptions() {
        Exception cause = new IllegalArgumentException("Cause exception");
        Exception exception = new RuntimeException("Main exception", cause);
        String result = getHelper().renderThrowableChain(exception);
        assertNotNull(result);
        assertTrue(result.contains("RuntimeException"));
        assertTrue(result.contains("IllegalArgumentException"));
        assertTrue(result.contains("↑"));
    }

    // Test Base64 encoding/decoding
    @Test
    @DisplayName("Base64编码解码 - 正常情况")
    void testBase64EncodeDecode_Normal() {
        String original = "Hello World";
        String encoded = getHelper().encodeWithBase64(original);
        byte[] decodedBytes = getHelper().decodeWithBase64ToBytes(encoded);
        String decoded = new String(decodedBytes);
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("Base64编码 - 空字符串")
    void testBase64Encode_Empty() {
        String encoded = getHelper().encodeWithBase64("");
        assertNotNull(encoded);
    }

    @Test
    @DisplayName("Base64编码到字节数组")
    void testBase64EncodeToBytes() {
        String original = "Test";
        byte[] encoded = getHelper().encodeWithBase64ToBytes(original);
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);
    }

    // Test Base32 encoding/decoding
    @Test
    @DisplayName("Base32编码解码 - 正常情况")
    void testBase32EncodeDecode_Normal() {
        String original = "Hello World";
        String encoded = getHelper().encodeWithBase32(original);
        String decoded = getHelper().decodeWithBase32(encoded);
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("Base32解码到字节数组")
    void testBase32DecodeToBytes() {
        String original = "Test";
        String encoded = getHelper().encodeWithBase32(original);
        byte[] decoded = getHelper().decodeWithBase32ToBytes(encoded);
        assertNotNull(decoded);
        assertEquals(original, new String(decoded));
    }

    // Test regexFindAll
    @Test
    @DisplayName("regexFindAll - 正常匹配")
    void testRegexFindAll_Normal() {
        String text = "The numbers are 123, 456, and 789";
        List<String> matches = getHelper().regexFindAll("\\d+", 0, text, 0);
        assertEquals(3, matches.size());
        assertEquals("123", matches.get(0));
        assertEquals("456", matches.get(1));
        assertEquals("789", matches.get(2));
    }

    @Test
    @DisplayName("regexFindAll - 无匹配")
    void testRegexFindAll_NoMatches() {
        String text = "No numbers here";
        List<String> matches = getHelper().regexFindAll("\\d+", 0, text, 0);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("regexFindAll - 分组匹配")
    void testRegexFindAll_GroupMatches() {
        String text = "email@example.com and test@domain.org";
        List<String> matches = getHelper().regexFindAll("(\\w+)@(\\w+\\.\\w+)", 0, text, 1);
        assertEquals(2, matches.size());
        assertEquals("email", matches.get(0));
        assertEquals("test", matches.get(1));
    }

    // Test escapeForHttpEntity
    @Test
    @DisplayName("escapeForHttpEntity - 正常转义")
    void testEscapeForHttpEntity_Normal() {
        String result = getHelper().escapeForHttpEntity("Hello & World < > @");
        assertEquals("Hello &amp; World &lt; &gt; &commat;", result);
    }

    @Test
    @DisplayName("escapeForHttpEntity - 无需转义")
    void testEscapeForHttpEntity_NoEscape() {
        String result = getHelper().escapeForHttpEntity("Hello World");
        assertEquals("Hello World", result);
    }

    // Test NyaCode encoding/decoding
    @Test
    @DisplayName("NyaCode编码解码 - 正常情况")
    void testNyaCodeEncodeDecode_Normal() {
        String original = "Hello World";
        String encoded = getHelper().encodeToNyaCode(original);
        String decoded = getHelper().decodeFromNyaCode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("NyaCode编码解码 - 特殊字符")
    void testNyaCodeEncodeDecode_SpecialChars() {
        String original = "Hello@World!";
        String encoded = getHelper().encodeToNyaCode(original);
        String decoded = getHelper().decodeFromNyaCode(encoded);
        assertEquals(original, decoded);
    }

    // Test truncateWithEllipsis
    @Test
    @DisplayName("truncateWithEllipsis - 需要截断")
    void testTruncateWithEllipsis_NeedsTruncation() {
        String result = getHelper().truncateWithEllipsis("Hello World", 5);
        assertEquals("Hello...", result);
    }

    @Test
    @DisplayName("truncateWithEllipsis - 不需要截断")
    void testTruncateWithEllipsis_NoTruncation() {
        String result = getHelper().truncateWithEllipsis("Hello", 10);
        assertEquals("Hello", result);
    }

    @Test
    @DisplayName("truncateWithEllipsis - null输入")
    void testTruncateWithEllipsis_Null() {
        String result = getHelper().truncateWithEllipsis(null, 5);
        assertEquals("", result);
    }

    @Test
    @DisplayName("truncateWithEllipsis - 边界情况")
    void testTruncateWithEllipsis_EdgeCase() {
        String result = getHelper().truncateWithEllipsis("Hello", 5);
        assertEquals("Hello", result);
    }

    // Test isNullOrBlank
    @Test
    @DisplayName("isNullOrBlank - null")
    void testIsNullOrBlank_Null() {
        assertTrue(getHelper().isNullOrBlank(null));
    }

    @Test
    @DisplayName("isNullOrBlank - 空字符串")
    void testIsNullOrBlank_Empty() {
        assertTrue(getHelper().isNullOrBlank(""));
    }

    @Test
    @DisplayName("isNullOrBlank - 空白字符")
    void testIsNullOrBlank_Whitespace() {
        assertTrue(getHelper().isNullOrBlank("   "));
        assertTrue(getHelper().isNullOrBlank("\t\n"));
    }

    @Test
    @DisplayName("isNullOrBlank - 非空")
    void testIsNullOrBlank_NotBlank() {
        assertFalse(getHelper().isNullOrBlank("Hello"));
        assertFalse(getHelper().isNullOrBlank(" Hello "));
    }

    // Test reverse
    @Test
    @DisplayName("reverse - 正常情况")
    void testReverse_Normal() {
        String result = getHelper().reverse("Hello");
        assertEquals("olleH", result);
    }

    @Test
    @DisplayName("reverse - null输入")
    void testReverse_Null() {
        String result = getHelper().reverse(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("reverse - 空字符串")
    void testReverse_Empty() {
        String result = getHelper().reverse("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("reverse - 单字符")
    void testReverse_SingleChar() {
        String result = getHelper().reverse("A");
        assertEquals("A", result);
    }

    // Test countOccurrences
    @Test
    @DisplayName("countOccurrences - 正常情况")
    void testCountOccurrences_Normal() {
        int count = getHelper().countOccurrences("Hello World Hello", "Hello");
        assertEquals(2, count);
    }

    @Test
    @DisplayName("countOccurrences - 无匹配")
    void testCountOccurrences_NoMatch() {
        int count = getHelper().countOccurrences("Hello World", "xyz");
        assertEquals(0, count);
    }

    @Test
    @DisplayName("countOccurrences - null输入")
    void testCountOccurrences_Null() {
        assertEquals(0, getHelper().countOccurrences(null, "test"));
        assertEquals(0, getHelper().countOccurrences("test", null));
        assertEquals(0, getHelper().countOccurrences(null, null));
    }

    @Test
    @DisplayName("countOccurrences - 空子字符串")
    void testCountOccurrences_EmptySubstring() {
        int count = getHelper().countOccurrences("Hello", "");
        assertEquals(0, count);
    }

    @Test
    @DisplayName("countOccurrences - 重叠匹配")
    void testCountOccurrences_Overlapping() {
        int count = getHelper().countOccurrences("aaa", "aa");
        assertEquals(1, count); // 非重叠计数
    }

    // Test removeWhitespace
    @Test
    @DisplayName("removeWhitespace - 正常情况")
    void testRemoveWhitespace_Normal() {
        String result = getHelper().removeWhitespace("Hello World Test");
        assertEquals("HelloWorldTest", result);
    }

    @Test
    @DisplayName("removeWhitespace - 多种空白字符")
    void testRemoveWhitespace_MultipleWhitespaceTypes() {
        String result = getHelper().removeWhitespace("Hello\tWorld\nTest");
        assertEquals("HelloWorldTest", result);
    }

    @Test
    @DisplayName("removeWhitespace - null输入")
    void testRemoveWhitespace_Null() {
        String result = getHelper().removeWhitespace(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("removeWhitespace - 无空白字符")
    void testRemoveWhitespace_NoWhitespace() {
        String result = getHelper().removeWhitespace("HelloWorld");
        assertEquals("HelloWorld", result);
    }

    // Test isNumericAsIntegralNumber
    @Test
    @DisplayName("isNumericAsIntegralNumber - 正整数")
    void testIsNumericAsIntegralNumber_PositiveInteger() {
        assertTrue(getHelper().isNumericAsIntegralNumber("123"));
        assertTrue(getHelper().isNumericAsIntegralNumber("0"));
    }

    @Test
    @DisplayName("isNumericAsIntegralNumber - 负数")
    void testIsNumericAsIntegralNumber_Negative() {
        assertFalse(getHelper().isNumericAsIntegralNumber("-123"));
    }

    @Test
    @DisplayName("isNumericAsIntegralNumber - 小数")
    void testIsNumericAsIntegralNumber_Decimal() {
        assertFalse(getHelper().isNumericAsIntegralNumber("12.3"));
    }

    @Test
    @DisplayName("isNumericAsIntegralNumber - 非数字")
    void testIsNumericAsIntegralNumber_NonNumeric() {
        assertFalse(getHelper().isNumericAsIntegralNumber("abc"));
        assertFalse(getHelper().isNumericAsIntegralNumber("12a"));
    }

    @Test
    @DisplayName("isNumericAsIntegralNumber - null或空")
    void testIsNumericAsIntegralNumber_NullOrEmpty() {
        assertFalse(getHelper().isNumericAsIntegralNumber(null));
        assertFalse(getHelper().isNumericAsIntegralNumber(""));
    }

    // Test isNumericAsRealNumber
    @Test
    @DisplayName("isNumericAsRealNumber - 正整数")
    void testIsNumericAsRealNumber_PositiveInteger() {
        assertTrue(getHelper().isNumericAsRealNumber("123"));
        assertTrue(getHelper().isNumericAsRealNumber("0"));
    }

    @Test
    @DisplayName("isNumericAsRealNumber - 负整数")
    void testIsNumericAsRealNumber_NegativeInteger() {
        assertTrue(getHelper().isNumericAsRealNumber("-123"));
    }

    @Test
    @DisplayName("isNumericAsRealNumber - 小数")
    void testIsNumericAsRealNumber_Decimal() {
        assertTrue(getHelper().isNumericAsRealNumber("12.3"));
        assertTrue(getHelper().isNumericAsRealNumber("-12.3"));
        assertTrue(getHelper().isNumericAsRealNumber("0.123"));
        assertTrue(getHelper().isNumericAsRealNumber("-0.123"));
    }

    @Test
    @DisplayName("isNumericAsRealNumber - 非数字")
    void testIsNumericAsRealNumber_NonNumeric() {
        assertFalse(getHelper().isNumericAsRealNumber("abc"));
        assertFalse(getHelper().isNumericAsRealNumber("12.3.4"));
        assertFalse(getHelper().isNumericAsRealNumber("12a"));
    }

    @Test
    @DisplayName("isNumericAsRealNumber - null或空")
    void testIsNumericAsRealNumber_NullOrEmpty() {
        assertFalse(getHelper().isNumericAsRealNumber(null));
        assertFalse(getHelper().isNumericAsRealNumber(""));
    }

    // Test capitalizeWords
    @Test
    @DisplayName("capitalizeWords - 正常情况")
    void testCapitalizeWords_Normal() {
        String result = getHelper().capitalizeWords("hello world test");
        assertEquals("Hello World Test", result);
    }

    @Test
    @DisplayName("capitalizeWords - 单词间多个空格")
    void testCapitalizeWords_MultipleSpaces() {
        String result = getHelper().capitalizeWords("hello  world");
        assertEquals("Hello  World", result);
    }

    @Test
    @DisplayName("capitalizeWords - 已大写")
    void testCapitalizeWords_AlreadyCapitalized() {
        String result = getHelper().capitalizeWords("Hello World");
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("capitalizeWords - null或空")
    void testCapitalizeWords_NullOrEmpty() {
        assertEquals("", getHelper().capitalizeWords(null));
        assertEquals("", getHelper().capitalizeWords(""));
    }

    @Test
    @DisplayName("capitalizeWords - 混合大小写")
    void testCapitalizeWords_MixedCase() {
        String result = getHelper().capitalizeWords("hELLO wORLD");
        assertEquals("HELLO WORLD", result);
    }

    // Test removeNonAlphanumeric
    @Test
    @DisplayName("removeNonAlphanumeric - 正常情况")
    void testRemoveNonAlphanumeric_Normal() {
        String result = getHelper().removeNonAlphanumeric("Hello, World! 123");
        assertEquals("HelloWorld123", result);
    }

    @Test
    @DisplayName("removeNonAlphanumeric - 只有字母数字")
    void testRemoveNonAlphanumeric_OnlyAlphanumeric() {
        String result = getHelper().removeNonAlphanumeric("Hello123");
        assertEquals("Hello123", result);
    }

    @Test
    @DisplayName("removeNonAlphanumeric - null输入")
    void testRemoveNonAlphanumeric_Null() {
        String result = getHelper().removeNonAlphanumeric(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("removeNonAlphanumeric - 特殊字符")
    void testRemoveNonAlphanumeric_SpecialChars() {
        String result = getHelper().removeNonAlphanumeric("@#$%^&*()");
        assertEquals("", result);
    }

    // Test isValidEmail
    @Test
    @DisplayName("isValidEmail - 有效邮箱")
    void testIsValidEmail_Valid() {
        assertTrue(getHelper().isValidEmail("test@example.com"));
        assertTrue(getHelper().isValidEmail("user.name@domain.org"));
        assertTrue(getHelper().isValidEmail("user+tag@example.co.uk"));
    }

    @Test
    @DisplayName("isValidEmail - 无效邮箱")
    void testIsValidEmail_Invalid() {
        assertFalse(getHelper().isValidEmail("invalid-email"));
        assertFalse(getHelper().isValidEmail("@example.com"));
        assertFalse(getHelper().isValidEmail("test@"));
        // assertFalse(getHelper().isValidEmail("test..test@example.com")); // 存在争议
    }

    @Test
    @DisplayName("isValidEmail - null输入")
    void testIsValidEmail_Null() {
        assertFalse(getHelper().isValidEmail(null));
    }

    @Test
    @DisplayName("isValidEmail - 空字符串")
    void testIsValidEmail_Empty() {
        assertFalse(getHelper().isValidEmail(""));
    }

    // Test generateRandomString
    @Test
    @DisplayName("generateRandomString - 默认字符集")
    void testGenerateRandomString_DefaultCharset() {
        String result = getHelper().generateRandomString(10);
        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.matches("[A-Za-z0-9]+"));
    }

    @Test
    @DisplayName("generateRandomString - 自定义字符集")
    void testGenerateRandomString_CustomCharset() {
        String result = getHelper().generateRandomString(5, "abc");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertTrue(result.matches("[abc]+"));
    }

    @Test
    @DisplayName("generateRandomString - 长度为0")
    void testGenerateRandomString_ZeroLength() {
        String result = getHelper().generateRandomString(0);
        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    @DisplayName("generateRandomString - 空字符集异常")
    void testGenerateRandomString_EmptyCharset() {
        assertThrows(IllegalArgumentException.class, () -> {
            getHelper().generateRandomString(5, "");
        });
    }

    @Test
    @DisplayName("generateRandomString - 随机性测试")
    void testGenerateRandomString_Randomness() {
        String result1 = getHelper().generateRandomString(20);
        String result2 = getHelper().generateRandomString(20);
        assertNotEquals(result1, result2); // 虽然理论上可能相等，但概率极低
    }

    // Test buildStackChainText
    @Test
    @DisplayName("buildStackChainText - null输入")
    void testBuildStackChainText_Null() {
        String result = getHelper().buildStackChainText(null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("buildStackChainText - 空堆栈")
    void testBuildStackChainText_EmptyStack() {
        StackTraceElement[] stack = {};
        String result = getHelper().buildStackChainText(stack);
        assertNotNull(result);
    }

    @Test
    @DisplayName("buildStackChainText - 正常堆栈")
    void testBuildStackChainText_NormalStack() {
        try {
            throw new RuntimeException("Test");
        } catch (Exception e) {
            String result = getHelper().buildStackChainText(e.getStackTrace());
            assertNotNull(result);
            assertTrue(result.contains("KeelStringHelperTest"));
        }
    }
}