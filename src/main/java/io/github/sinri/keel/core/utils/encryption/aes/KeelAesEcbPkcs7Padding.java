package io.github.sinri.keel.core.utils.encryption.aes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

/**
 * @since 5.0.0
 */
public class KeelAesEcbPkcs7Padding extends KeelAesUsingPkcs7Padding {

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesEcbPkcs7Padding(String key) {
        super(key);
    }

    @Override
    public SupportedCipherAlgorithm getCipherAlgorithm() {
        return SupportedCipherAlgorithm.AesEcbPkcs7Padding;
    }

    /**
     * AES加密
     *
     * @param source 源字符串
     * @return 加密后的密文
     *  使用本方法需要加载BouncyCastle相关的库！
     */
    public String encrypt(String source) {
        try {
            byte[] sourceBytes = source.getBytes(ENCODING);
            byte[] keyBytes = getKey().getBytes(ENCODING);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression(), "BC");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(sourceBytes);
            return Base64.getEncoder().encodeToString(decrypted);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptStr 加密后的密文
     * @return 源字符串
     */
    public String decrypt(String encryptStr) {
        try {
            byte[] sourceBytes = Base64.getDecoder().decode(encryptStr);
            byte[] keyBytes = getKey().getBytes(ENCODING);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression(), "BC");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = cipher.doFinal(sourceBytes);
            return new String(decoded, ENCODING);
        } catch (NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | NoSuchProviderException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
