package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.encryption.base32.Base32;
import io.vertx.core.buffer.Buffer;
import org.jspecify.annotations.NullMarked;

import java.util.Base64;

/**
 * 二进制工具类。
 *
 * @since 5.0.0
 */
@NullMarked
public class BinaryUtils {
    final static char[] HEX_DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    final static char[] HEX_DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private BinaryUtils() {
    }

    private static String encodeHexWithDigits(final char[] HEX_DIGITS, Buffer buffer, int since, int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = since; i < since + length; i++) {
            hex
                    .append(HEX_DIGITS[(0xF0 & buffer.getByte(i)) >>> 4])
                    .append(HEX_DIGITS[0x0F & buffer.getByte(i)])
            ;
        }
        return hex.toString();
    }

    /**
     * 将字节数组编码为十六进制字符串（使用小写字母）。
     * <p>将每个字节转换为两位十六进制字符，使用小写字母（0-9, a-f）。
     *
     * @param data 待编码的字节数组
     * @return 十六进制字符串（小写）
     */
    public static String encodeHexWithLowerDigits(final byte[] data) {
        return encodeHexWithLowerDigits(Buffer.buffer(data));
    }

    /**
     * 将 Vert.x Buffer 编码为十六进制字符串（使用小写字母）。
     * <p>将 Buffer 中的所有字节转换为十六进制字符串，使用小写字母（0-9, a-f）。
     *
     * @param buffer Vert.x 的 Buffer 实例
     * @return 十六进制字符串（小写）
     */
    public static String encodeHexWithLowerDigits(Buffer buffer) {
        return encodeHexWithLowerDigits(buffer, 0, buffer.length());
    }

    /**
     * 将 Vert.x Buffer 的指定部分编码为十六进制字符串（使用小写字母）。
     * <p>从指定起始位置开始，将指定长度的字节转换为十六进制字符串，使用小写字母（0-9, a-f）。
     *
     * @param buffer Vert.x 的 Buffer 实例
     * @param since  起始索引位置
     * @param length 要编码的字节长度
     * @return 十六进制字符串（小写）
     */
    public static String encodeHexWithLowerDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_LOWER, buffer, since, length);
    }

    /**
     * 将字节数组编码为十六进制字符串（使用大写字母）。
     * <p>将每个字节转换为两位十六进制字符，使用大写字母（0-9, A-F）。
     *
     * @param data 待编码的字节数组
     * @return 十六进制字符串（大写）
     */
    public static String encodeHexWithUpperDigits(final byte[] data) {
        return encodeHexWithUpperDigits(Buffer.buffer(data));
    }

    /**
     * 将 Vert.x Buffer 编码为十六进制字符串（使用大写字母）。
     * <p>将 Buffer 中的所有字节转换为十六进制字符串，使用大写字母（0-9, A-F）。
     *
     * @param buffer Vert.x 的 Buffer 实例
     * @return 十六进制字符串（大写）
     */
    public static String encodeHexWithUpperDigits(Buffer buffer) {
        return encodeHexWithUpperDigits(buffer, 0, buffer.length());
    }

    /**
     * 将 Vert.x Buffer 的指定部分编码为十六进制字符串（使用大写字母）。
     * <p>从指定起始位置开始，将指定长度的字节转换为十六进制字符串，使用大写字母（0-9, A-F）。
     *
     * @param buffer Vert.x 的 Buffer 实例
     * @param since  起始索引位置
     * @param length 要编码的字节长度
     * @return 十六进制字符串（大写）
     */
    public static String encodeHexWithUpperDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_UPPER, buffer, since, length);
    }

    /**
     * 使用 Base64 解码字节数组。
     * <p>将 Base64 编码的字节数组解码为原始字节数组。
     *
     * @param bytes Base64 编码的字节数组
     * @return 解码后的字节数组
     */
    public static byte[] decodeWithBase64(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    /**
     * 使用 Base64 编码字节数组。
     * <p>将字节数组编码为 Base64 格式的字节数组。
     *
     * @param bytes 待编码的字节数组
     * @return Base64 编码后的字节数组
     */
    public static byte[] encodeWithBase64(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * 使用 Base64 编码字节数组并转换为字符串。
     * <p>将字节数组编码为 Base64 格式的字符串。
     *
     * @param bytes 待编码的字节数组
     * @return Base64 编码后的字符串
     */

    public static String encodeWithBase64ToString(byte[] bytes) {
        return new String(encodeWithBase64(bytes));
    }

    /**
     * 使用 Base32 编码字节数组。
     * <p>将字节数组编码为 Base32 格式的字节数组。
     *
     * @param bytes 待编码的字节数组
     * @return Base32 编码后的字节数组
     */
    public static byte[] encodeWithBase32(byte[] bytes) {
        return encodeWithBase32ToString(bytes).getBytes();
    }

    /**
     * 使用 Base32 编码字节数组并转换为字符串。
     * <p>将字节数组编码为 Base32 格式的字符串。
     *
     * @param bytes 待编码的字节数组
     * @return Base32 编码后的字符串
     */

    public static String encodeWithBase32ToString(byte[] bytes) {
        return Base32.encode(bytes);
    }

    /**
     * 使用 Base32 解码字节数组。
     * <p>将 Base32 编码的字节数组解码为原始字节数组。
     *
     * @param bytes Base32 编码的字节数组
     * @return 解码后的字节数组
     */
    public static byte[] decodeWithBase32(byte[] bytes) {
        return Base32.decode(new String(bytes));
    }

    /**
     * 使用 Base32 解码字节数组并转换为字符串。
     * <p>将 Base32 编码的字节数组解码为原始字节数组，然后转换为字符串。
     *
     * @param bytes Base32 编码的字节数组
     * @return 解码后的字符串
     */
    public static String decodeWithBase32ToString(byte[] bytes) {
        return new String(decodeWithBase32(bytes));
    }
}
