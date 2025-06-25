package io.github.sinri.keel.core.helper;

import io.netty.util.concurrent.FastThreadLocal;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


/**
 * 校验用加密（摘要）。
 * 特征是不可解密。
 *
 * @since 2.8
 */
public class KeelDigestHelper {

    public static final String DIGEST_ALGO_SHA_512 = "SHA-512";
    public static final String DIGEST_ALGO_MD5 = "MD5";
    public static final String DIGEST_ALGO_SHA_1 = "SHA";
    private static final KeelDigestHelper instance = new KeelDigestHelper();

    private KeelDigestHelper() {

    }

    static KeelDigestHelper getInstance() {
        return instance;
    }

    /**
     * As of 4.1.0, it is open to the world.
     *
     * @since 4.0.0 <a href="https://github.com/sinri/Keel/pull/22">Enhance MD5 Performance with FastThreadLocal
     *         #22</a>
     */
    public static MessageDigest getMD5MessageDigest() {
        return MD5.HOLDER.get();
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     * As of 4.1.0, be realized by {@link KeelDigestHelper#md5(byte[])}
     *
     * @param raw raw string
     * @return md5 with lower digits
     * @since 1.1
     * @since 4.0.0 <a href="https://github.com/sinri/Keel/pull/22">Enhance MD5 Performance with FastThreadLocal
     *         #22</a>
     */
    @Nonnull
    public String md5(@Nonnull String raw) {
        return md5(raw.getBytes());
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     *
     * @since 4.1.0
     */
    public String md5(@Nonnull byte[] raw) {
        MessageDigest digest = KeelDigestHelper.getMD5MessageDigest();
        byte[] digested = digest.digest(raw);
        return Keel.binaryHelper().encodeHexWithLowerDigits(digested);
    }

    /**
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     * As of 4.1.0, realized by {@link KeelDigestHelper#MD5(byte[])}.
     *
     * @param raw raw string
     * @return MD5 with upper digits
     * @since 1.1
     * @since 4.0.0 <a href="https://github.com/sinri/Keel/pull/22">Enhance MD5 Performance with FastThreadLocal
     *         #22</a>
     */
    @Nonnull
    public String MD5(@Nonnull String raw) {
        return MD5(raw.getBytes());
    }

    /**
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     *
     * @since 4.1.0
     */
    public String MD5(@Nonnull byte[] raw) {
        MessageDigest digest = KeelDigestHelper.getMD5MessageDigest();
        byte[] digested = digest.digest(raw);
        return Keel.binaryHelper().encodeHexWithUpperDigits(digested);
    }

    /**
     * @since 4.1.0
     */
    public String digestToLower(@Nonnull String algorithm, @Nonnull byte[] raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(raw);
        return Keel.binaryHelper().encodeHexWithLowerDigits(md.digest());
    }

    /**
     * As of 4.1.0, realized by {@link KeelDigestHelper#digestToLower(String, byte[])}.
     *
     * @since 3.0.11
     */
    public String digestToLower(@Nonnull String algorithm, @Nonnull String raw) throws NoSuchAlgorithmException {
        return digestToLower(algorithm, raw.getBytes());
    }

    /**
     * @since 4.1.0
     */
    public String digestToUpper(@Nonnull String algorithm, @Nonnull byte[] raw) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(raw);
        return Keel.binaryHelper().encodeHexWithUpperDigits(md.digest());
    }

    /**
     * As of 4.1.0, realized by {@link KeelDigestHelper#digestToUpper(String, byte[])}.
     *
     * @since 3.0.11
     */
    public String digestToUpper(@Nonnull String algorithm, @Nonnull String raw) throws NoSuchAlgorithmException {
        return digestToUpper(algorithm, raw.getBytes());
    }

    /**
     * @since 3.0.11
     */
    @Nonnull
    public String SHA512(@Nonnull String raw) {
        try {
            return digestToUpper(DIGEST_ALGO_SHA_512, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.11
     */
    @Nonnull
    public String sha512(@Nonnull String raw) {
        try {
            return digestToLower(DIGEST_ALGO_SHA_512, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String SHA1(@Nonnull String raw) {
        try {
            return digestToUpper(DIGEST_ALGO_SHA_1, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String sha1(@Nonnull String raw) {
        try {
            return digestToLower(DIGEST_ALGO_SHA_1, raw);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.8
     */
    @Nonnull
    private byte[] compute_hmac_sha1(@Nonnull String raw, @Nonnull String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String MAC_NAME = "HmacSHA1";
        String ENCODING = "UTF-8";

        byte[] data = key.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二个参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = raw.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String hmac_sha1_base64(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @since 2.8
     */
    @Nonnull
    public String hmac_sha1_hex(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Keel.binaryHelper().encodeHexWithLowerDigits(bytes);
    }

    /**
     * @since 2.8
     */
    public @Nonnull String HMAC_SHA1_HEX(@Nonnull String raw, @Nonnull String key) {
        byte[] bytes;
        try {
            bytes = compute_hmac_sha1(raw, key);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Keel.binaryHelper().encodeHexWithUpperDigits(bytes);
    }

    /**
     * @since 4.0.0 <a href="https://github.com/sinri/Keel/pull/22">Enhance MD5 Performance with FastThreadLocal
     *         #22</a>
     */
    private static class MD5 {

        private static final FastThreadLocal<MessageDigest> HOLDER = new FastThreadLocal<>() {
            @Override
            protected MessageDigest initialValue() throws Exception {
                return MessageDigest.getInstance(DIGEST_ALGO_MD5);
            }
        };
    }
}
