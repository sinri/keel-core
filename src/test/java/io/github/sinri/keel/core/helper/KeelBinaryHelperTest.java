package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelBinaryHelperTest extends KeelJUnit5Test {

    public KeelBinaryHelperTest(Vertx vertx) {
        super(vertx);
    }

    private KeelBinaryHelper getHelper() {
        return Keel.binaryHelper();
    }

    // Test encodeHexWithLowerDigits methods
    @Test
    @DisplayName("encodeHexWithLowerDigits - 字节数组版本")
    void testEncodeHexWithLowerDigits_ByteArray() {
        byte[] data = {0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF};
        String result = getHelper().encodeHexWithLowerDigits(data);
        assertEquals("01020a0fff", result);
    }

    @Test
    @DisplayName("encodeHexWithLowerDigits - Buffer版本")
    void testEncodeHexWithLowerDigits_Buffer() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF});
        String result = getHelper().encodeHexWithLowerDigits(buffer);
        assertEquals("01020a0fff", result);
    }

    @Test
    @DisplayName("encodeHexWithLowerDigits - Buffer范围版本")
    void testEncodeHexWithLowerDigits_BufferRange() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF});
        String result = getHelper().encodeHexWithLowerDigits(buffer, 1, 3);
        assertEquals("020a0f", result);
    }

    @Test
    @DisplayName("encodeHexWithLowerDigits - 空Buffer")
    void testEncodeHexWithLowerDigits_EmptyBuffer() {
        Buffer buffer = Buffer.buffer();
        String result = getHelper().encodeHexWithLowerDigits(buffer);
        assertEquals("", result);
    }

    // Test encodeHexWithUpperDigits methods
    @Test
    @DisplayName("encodeHexWithUpperDigits - 字节数组版本")
    void testEncodeHexWithUpperDigits_ByteArray() {
        byte[] data = {0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF};
        String result = getHelper().encodeHexWithUpperDigits(data);
        assertEquals("01020A0FFF", result);
    }

    @Test
    @DisplayName("encodeHexWithUpperDigits - Buffer版本")
    void testEncodeHexWithUpperDigits_Buffer() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF});
        String result = getHelper().encodeHexWithUpperDigits(buffer);
        assertEquals("01020A0FFF", result);
    }

    @Test
    @DisplayName("encodeHexWithUpperDigits - Buffer范围版本")
    void testEncodeHexWithUpperDigits_BufferRange() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x0A, 0x0F, (byte) 0xFF});
        String result = getHelper().encodeHexWithUpperDigits(buffer, 1, 3);
        assertEquals("020A0F", result);
    }

    @Test
    @DisplayName("encodeHexWithUpperDigits - 所有字节值测试")
    void testEncodeHexWithUpperDigits_AllByteValues() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        String result = getHelper().encodeHexWithUpperDigits(data);
        // 验证长度
        assertEquals(512, result.length());
        // 验证特定值
        assertTrue(result.startsWith("000102"));
        assertTrue(result.endsWith("FDFEFF"));
    }

    // Test Base64 encoding/decoding
    @Test
    @DisplayName("Base64编码解码 - 往返测试")
    void testBase64EncodeDecodeRoundtrip() {
        byte[] original = "Hello World! 测试数据".getBytes();
        byte[] encoded = getHelper().encodeWithBase64(original);
        byte[] decoded = getHelper().decodeWithBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    @DisplayName("Base64编码为字符串")
    void testBase64EncodeToString() {
        byte[] data = "Hello".getBytes();
        String result = getHelper().encodeWithBase64ToString(data);
        assertNotNull(result);
        assertEquals("SGVsbG8=", result);
    }

    @Test
    @DisplayName("Base64编码解码 - 空数据")
    void testBase64EncodeDecodeEmpty() {
        byte[] empty = new byte[0];
        byte[] encoded = getHelper().encodeWithBase64(empty);
        byte[] decoded = getHelper().decodeWithBase64(encoded);
        assertEquals(0, decoded.length);
    }

    @Test
    @DisplayName("Base64编码解码 - 二进制数据")
    void testBase64EncodeDecodeBinary() {
        byte[] binaryData = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x01, (byte) 0x80};
        byte[] encoded = getHelper().encodeWithBase64(binaryData);
        byte[] decoded = getHelper().decodeWithBase64(encoded);
        assertArrayEquals(binaryData, decoded);
    }

    // Test Base32 encoding/decoding
    @Test
    @DisplayName("Base32编码解码 - 往返测试")
    void testBase32EncodeDecodeRoundtrip() {
        byte[] original = "Hello World!".getBytes();
        byte[] encoded = getHelper().encodeWithBase32(original);
        byte[] decoded = getHelper().decodeWithBase32(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    @DisplayName("Base32编码为字符串")
    void testBase32EncodeToString() {
        byte[] data = "Hello".getBytes();
        String result = getHelper().encodeWithBase32ToString(data);
        assertNotNull(result);
        assertTrue(result.matches("[A-Z2-7=]*"));
    }

    @Test
    @DisplayName("Base32解码为字符串")
    void testBase32DecodeToString() {
        byte[] original = "Hello World".getBytes();
        String encoded = getHelper().encodeWithBase32ToString(original);
        String decoded = getHelper().decodeWithBase32ToString(encoded.getBytes());
        assertEquals("Hello World", decoded);
    }

    @Test
    @DisplayName("Base32编码解码 - 空数据")
    void testBase32EncodeDecodeEmpty() {
        byte[] empty = new byte[0];
        byte[] encoded = getHelper().encodeWithBase32(empty);
        byte[] decoded = getHelper().decodeWithBase32(encoded);
        assertEquals(0, decoded.length);
    }

    @Test
    @DisplayName("Base32编码解码 - 各种长度数据")
    void testBase32EncodeDecodeVariousLengths() {
        for (int length = 1; length <= 10; length++) {
            byte[] data = new byte[length];
            for (int i = 0; i < length; i++) {
                data[i] = (byte) (i + 65); // A, B, C, etc.
            }
            byte[] encoded = getHelper().encodeWithBase32(data);
            byte[] decoded = getHelper().decodeWithBase32(encoded);
            assertArrayEquals(data, decoded, "Failed for length: " + length);
        }
    }

    // Test edge cases and error conditions
    @Test
    @DisplayName("十六进制编码 - 边界条件")
    void testHexEncodingBoundaryConditions() {
        // 测试单个字节
        byte[] singleByte = {(byte) 0x5A};
        assertEquals("5a", getHelper().encodeHexWithLowerDigits(singleByte));
        assertEquals("5A", getHelper().encodeHexWithUpperDigits(singleByte));
        
        // 测试零字节
        byte[] zeroByte = {0x00};
        assertEquals("00", getHelper().encodeHexWithLowerDigits(zeroByte));
        assertEquals("00", getHelper().encodeHexWithUpperDigits(zeroByte));
        
        // 测试最大字节值
        byte[] maxByte = {(byte) 0xFF};
        assertEquals("ff", getHelper().encodeHexWithLowerDigits(maxByte));
        assertEquals("FF", getHelper().encodeHexWithUpperDigits(maxByte));
    }

    @Test
    @DisplayName("Buffer范围编码 - 边界测试")
    void testBufferRangeEncodingBoundary() {
        Buffer buffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        
        // 测试起始位置
        assertEquals("01", getHelper().encodeHexWithLowerDigits(buffer, 0, 1));
        
        // 测试结束位置
        assertEquals("05", getHelper().encodeHexWithLowerDigits(buffer, 4, 1));
        
        // 测试中间范围
        assertEquals("0203", getHelper().encodeHexWithLowerDigits(buffer, 1, 2));
        
        // 测试完整范围
        assertEquals("0102030405", getHelper().encodeHexWithLowerDigits(buffer, 0, 5));
    }

    @Test
    @DisplayName("编码性能测试 - 大数据量")
    void testEncodingPerformance() {
        // 创建较大的数据集
        byte[] largeData = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        // 测试各种编码方式
        String hexLower = getHelper().encodeHexWithLowerDigits(largeData);
        String hexUpper = getHelper().encodeHexWithUpperDigits(largeData);
        String base64 = getHelper().encodeWithBase64ToString(largeData);
        String base32 = getHelper().encodeWithBase32ToString(largeData);
        
        // 验证结果长度
        assertEquals(2000, hexLower.length());
        assertEquals(2000, hexUpper.length());
        assertTrue(base64.length() > 0);
        assertTrue(base32.length() > 0);
        
        // 验证往返编码
        assertArrayEquals(largeData, getHelper().decodeWithBase64(base64.getBytes()));
        assertArrayEquals(largeData, getHelper().decodeWithBase32(base32.getBytes()));
    }
} 