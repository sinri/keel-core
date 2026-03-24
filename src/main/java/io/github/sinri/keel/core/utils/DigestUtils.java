package io.github.sinri.keel.core.utils;

import org.jspecify.annotations.NullMarked;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 摘要工具类。
 * <p>
 * 校验用摘要，特征是不可解密。
 *
 * @since 5.0.0
 */
@NullMarked
public class DigestUtils {
    public static final String DIGEST_ALGO_SHA_512 = "SHA-512";
    public static final String DIGEST_ALGO_MD5 = "MD5";
    public static final String DIGEST_ALGO_SHA_1 = "SHA";
    private static final Map<String, MessageDigest> messageDigestCache = new ConcurrentHashMap<>();

    private DigestUtils() {

    }

    public static MessageDigest getMessageDigest(String algorithm) {
        return Objects.requireNonNull(messageDigestCache.computeIfAbsent(algorithm, key -> {
            synchronized (messageDigestCache) {
                try {
                    return MessageDigest.getInstance(key);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * 计算以UTF-8编码的字符串的 MD5 摘要值（小写十六进制）。
     *
     * @param raw 原始字符串
     * @return MD5 摘要值，使用小写字母和数字的十六进制表示
     */
    public static String md5(String raw) {
        return md5(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字节数组的 MD5 摘要值（小写十六进制）。
     *
     * @param raw 原始字节数组
     * @return MD5 摘要值，使用小写字母和数字的十六进制表示
     */
    public static String md5(byte[] raw) {
        MessageDigest digest = getMessageDigest(DIGEST_ALGO_MD5);
        byte[] digested = digest.digest(raw);
        return BinaryUtils.encodeHexWithLowerDigits(digested);
    }

    /**
     * 计算以UTF-8编码的字符串的 MD5 摘要值（大写十六进制）。
     *
     * @param raw 原始字符串
     * @return MD5 摘要值，使用大写字母和数字的十六进制表示
     */

    public static String MD5(String raw) {
        return MD5(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字节数组的 MD5 摘要值（大写十六进制）。
     *
     * @param raw 原始字节数组
     * @return MD5 摘要值，使用大写字母和数字的十六进制表示
     */
    public static String MD5(byte[] raw) {
        MessageDigest digest = getMessageDigest(DIGEST_ALGO_MD5);
        byte[] digested = digest.digest(raw);
        return BinaryUtils.encodeHexWithUpperDigits(digested);
    }

    /**
     * 使用指定算法计算字节数组的摘要值（小写十六进制）。
     *
     * @param algorithm 摘要算法名称（如 "SHA-256", "SHA-512" 等）
     * @param raw       原始字节数组
     * @return 摘要值，使用小写字母和数字的十六进制表示
     * @throws NoSuchAlgorithmException 如果指定的算法不可用
     */
    public static String digestToLower(String algorithm, byte[] raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(raw);
        return BinaryUtils.encodeHexWithLowerDigits(md.digest());
    }

    /**
     * 使用指定算法计算以UTF-8编码的字符串的摘要值（小写十六进制）。
     *
     * @param algorithm 摘要算法名称（如 "SHA-256", "SHA-512" 等）
     * @param raw       原始字符串
     * @return 摘要值，使用小写字母和数字的十六进制表示
     * @throws NoSuchAlgorithmException 如果指定的算法不可用
     */
    public static String digestToLower(String algorithm, String raw) throws NoSuchAlgorithmException {
        return digestToLower(algorithm, raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用指定算法计算字节数组的摘要值（大写十六进制）。
     *
     * @param algorithm 摘要算法名称（如 "SHA-256", "SHA-512" 等）
     * @param raw       原始字节数组
     * @return 摘要值，使用大写字母和数字的十六进制表示
     * @throws NoSuchAlgorithmException 如果指定的算法不可用
     */
    public static String digestToUpper(String algorithm, byte[] raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(raw);
        return BinaryUtils.encodeHexWithUpperDigits(md.digest());
    }

    /**
     * 使用指定算法计算以UTF-8编码的字符串的摘要值（大写十六进制）。
     *
     * @param algorithm 摘要算法名称（如 "SHA-256", "SHA-512" 等）
     * @param raw       原始字符串
     * @return 摘要值，使用大写字母和数字的十六进制表示
     * @throws NoSuchAlgorithmException 如果指定的算法不可用
     */
    public static String digestToUpper(String algorithm, String raw) throws NoSuchAlgorithmException {
        return digestToUpper(algorithm, raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算以UTF-8编码的字符串的 SHA-512 摘要值（大写十六进制）。
     *
     * @param raw 原始字符串
     * @return SHA-512 摘要值，使用大写字母和数字的十六进制表示
     * @throws RuntimeException 如果 SHA-512 算法不可用
     */

    public static String SHA512(String raw) {
        try {
            return digestToUpper(DIGEST_ALGO_SHA_512, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算以UTF-8编码的字符串的 SHA-512 摘要值（小写十六进制）。
     *
     * @param raw 原始字符串
     * @return SHA-512 摘要值，使用小写字母和数字的十六进制表示
     * @throws RuntimeException 如果 SHA-512 算法不可用
     */

    public static String sha512(String raw) {
        try {
            return digestToLower(DIGEST_ALGO_SHA_512, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算以UTF-8编码的字符串的 SHA-1 摘要值（大写十六进制）。
     *
     * @param raw 原始字符串
     * @return SHA-1 摘要值，使用大写字母和数字的十六进制表示
     * @throws RuntimeException 如果 SHA-1 算法不可用
     */

    public static String SHA1(String raw) {
        try {
            return digestToUpper(DIGEST_ALGO_SHA_1, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算以UTF-8编码的字符串的 SHA-1 摘要值（小写十六进制）。
     *
     * @param raw 原始字符串
     * @return SHA-1 摘要值，使用小写字母和数字的十六进制表示
     * @throws RuntimeException 如果 SHA-1 算法不可用
     */

    public static String sha1(String raw) {
        try {
            return digestToLower(DIGEST_ALGO_SHA_1, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算字符串的 HMAC-SHA1 摘要值（原始字节数组）。
     * <p>
     * 使用 UTF-8 编码处理输入字符串和密钥。
     *
     * @param raw 原始字符串
     * @param key 密钥字符串
     * @return HMAC-SHA1 摘要的原始字节数组
     * @throws UnsupportedEncodingException 如果 UTF-8 编码不可用
     * @throws NoSuchAlgorithmException     如果 HMAC-SHA1 算法不可用
     * @throws InvalidKeyException          如果密钥无效
     */
    private static byte[] compute_hmac_sha1(String raw, String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String MAC_NAME = "HmacSHA1";

        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        //根据给定的字节数组构造一个密钥,第二个参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = raw.getBytes(StandardCharsets.UTF_8);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    /**
     * 计算字符串的 HMAC-SHA1 摘要值（Base64 编码）。
     *
     * @param raw 原始字符串
     * @param key 密钥字符串
     * @return HMAC-SHA1 摘要值，使用 Base64 编码
     * @throws RuntimeException 如果计算过程中发生异常
     */

    public static String hmac_sha1_base64(String raw, String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 计算字符串的 HMAC-SHA1 摘要值（小写十六进制）。以UTF-8编码处理。
     *
     * @param raw 原始字符串
     * @param key 密钥字符串
     * @return HMAC-SHA1 摘要值，使用小写字母和数字的十六进制表示
     * @throws RuntimeException 如果计算过程中发生异常
     */

    public static String hmac_sha1_hex(String raw, String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return BinaryUtils.encodeHexWithLowerDigits(bytes);
    }

    /**
     * 计算字符串的 HMAC-SHA1 摘要值（大写十六进制）。以UTF-8编码处理。
     *
     * @param raw 原始字符串
     * @param key 密钥字符串
     * @return HMAC-SHA1 摘要值，使用大写字母和数字的十六进制表示
     * @throws RuntimeException 如果计算过程中发生异常
     */
    public static String HMAC_SHA1_HEX(String raw, String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return BinaryUtils.encodeHexWithUpperDigits(bytes);
    }
}
