package io.github.sinri.keel.core.utils.encryption.rsa;


import io.github.sinri.keel.core.utils.BinaryUtils;
import io.github.sinri.keel.core.utils.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * @since 5.0.0
 */
public class KeelRSA extends KeelRSAKeyPair {
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    /**
     * @param plainTextData data to encrypt
     * @return encrypted data
     * @throws NoSuchPaddingException 没有这种补全之术
     * @throws NoSuchAlgorithmException  无此加密算法
     * @throws InvalidKeyException       加密公钥非法
     * @throws IllegalBlockSizeException 明文长度非法
     * @throws BadPaddingException       明文数据已损坏
     */
    public byte[] encryptWithPublicKey(byte[] plainTextData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, Objects.requireNonNull(getPublicKey()));
        return cipher.doFinal(plainTextData);
    }

    /**
     * @param plainTextData data to encrypt
     * @return encrypted data
     * @throws NoSuchPaddingException 没有这种补全之术
     * @throws NoSuchAlgorithmException  无此加密算法
     * @throws InvalidKeyException       加密私钥非法
     * @throws IllegalBlockSizeException 明文长度非法
     * @throws BadPaddingException       明文数据已损坏
     */
    public byte[] encryptWithPrivateKey(byte[] plainTextData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, Objects.requireNonNull(getPrivateKey()));
        return cipher.doFinal(plainTextData);
    }

    /**
     * @param cipherData encrypted data
     * @return decrypted data
     * @throws NoSuchPaddingException    没有这种补全之术
     * @throws NoSuchAlgorithmException  无此解密算法
     * @throws InvalidKeyException       解密私钥非法
     * @throws IllegalBlockSizeException 密文长度非法
     * @throws BadPaddingException       密文数据已损坏
     */
    public byte[] decryptWithPrivateKey(byte[] cipherData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, Objects.requireNonNull(getPrivateKey()));
        return cipher.doFinal(cipherData);
    }

    /**
     * @param cipherData encrypted data
     * @return decrypted data
     * @throws NoSuchPaddingException    没有这种补全之术
     * @throws NoSuchAlgorithmException  无此解密算法
     * @throws InvalidKeyException       解密公钥非法
     * @throws IllegalBlockSizeException 密文长度非法
     * @throws BadPaddingException       密文数据已损坏
     */
    public byte[] decryptWithPublicKey(byte[] cipherData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, Objects.requireNonNull(getPublicKey()));
        return cipher.doFinal(cipherData);
    }

    /**
     * 利用私钥进行RSA签名.
     *
     * @param content 待签名的数据块
     * @return RSA签名结果
     * @throws NoSuchAlgorithmException 无此加密算法
     * @throws InvalidKeySpecException 不对劲的密钥特性
     * @throws InvalidKeyException 不对劲的密钥
     * @throws SignatureException 签名异常
     */
    public String signWithPrivateKey(byte[] content) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] bytesOfPrivateKey = this.getPrivateKey().getEncoded();
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(bytesOfPrivateKey);
        KeyFactory keyf = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyf.generatePrivate(priPKCS8);
        java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(priKey);
        signature.update(content);
        byte[] signed = signature.sign();
        return BinaryUtils.encodeWithBase64ToString(signed);
    }

    /**
     * 利用公钥进行RSA签名校验。
     *
     * @param content 被签名的数据块
     * @param sign    RSA签名
     * @return 校验结果
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws InvalidKeySpecException 不对劲的密钥特性
     * @throws InvalidKeyException 不对劲的密钥
     * @throws SignatureException 签名不对劲
     */
    public boolean verifySignWithPublicKey(byte[] content, String sign) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] encodedKey = getPublicKey().getEncoded();
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

        java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
        signature.initVerify(pubKey);
        signature.update(content);

        return signature.verify(StringUtils.decodeWithBase64ToBytes(sign));
    }
}
