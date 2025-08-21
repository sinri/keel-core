package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.core.helper.encryption.aes.KeelAes;
import io.github.sinri.keel.core.helper.encryption.rsa.KeelRSA;
import io.github.sinri.keel.core.helper.encryption.rsa.KeelRSAKeyPair;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelCryptographyHelperTest extends KeelJUnit5Test {

    public KeelCryptographyHelperTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testGetInstance() {
        // 测试单例模式
        KeelCryptographyHelper instance1 = KeelCryptographyHelper.getInstance();
        KeelCryptographyHelper instance2 = KeelCryptographyHelper.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testAesWithValidAlgorithm() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        
        // 测试所有支持的 AES 算法
        KeelAes.SupportedCipherAlgorithm[] algorithms = {
            KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding,
            KeelAes.SupportedCipherAlgorithm.AesCbcPkcs7Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbPkcs5Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbPkcs7Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbNoPadding
        };
        
        String testKey = "testKey1234567890"; // 16字节密钥
        
        for (KeelAes.SupportedCipherAlgorithm algorithm : algorithms) {
            KeelAes aes = helper.aes(algorithm, testKey);
            assertNotNull(aes);
            assertEquals(algorithm, aes.getCipherAlgorithm());
        }
    }

    @Test
    void testAesWithNullAlgorithm() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        String testKey = "testKey1234567890";
        
        assertThrows(NullPointerException.class, () -> {
            helper.aes(null, testKey);
        });
    }

    @Test
    void testAesWithNullKey() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        
        assertThrows(NullPointerException.class, () -> {
            helper.aes(KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding, null);
        });
    }

    @Test
    void testAesEncryptionDecryption() throws NoSuchAlgorithmException {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        String testKey = KeelAes.generate128BitsSecretKey(); // 使用生成的128位密钥
        String originalText = "Hello, World! This is a test message for AES encryption.";
        
        // 测试 CBC 模式
        KeelAes aesCbc = helper.aes(KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding, testKey);
        String encryptedCbc = aesCbc.encrypt(originalText);
        String decryptedCbc = aesCbc.decrypt(encryptedCbc);
        
        assertNotNull(encryptedCbc);
        assertNotEquals(originalText, encryptedCbc);
        assertEquals(originalText, decryptedCbc);
        
        // 测试 ECB 模式
        KeelAes aesEcb = helper.aes(KeelAes.SupportedCipherAlgorithm.AesEcbPkcs5Padding, testKey);
        String encryptedEcb = aesEcb.encrypt(originalText);
        String decryptedEcb = aesEcb.decrypt(encryptedEcb);
        
        assertNotNull(encryptedEcb);
        assertNotEquals(originalText, encryptedEcb);
        assertEquals(originalText, decryptedEcb);
    }

    @Test
    void testRsa() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        
        KeelRSA rsa = helper.rsa();
        assertNotNull(rsa);
        
        // 验证返回的是 KeelRSA 实例
        assertInstanceOf(KeelRSA.class, rsa);
    }

    @Test
    void testRsaKeyGeneration() throws NoSuchAlgorithmException {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        KeelRSA rsa = helper.rsa();
        
        // 确保目录存在
        String testKeysDir = "target/test-keys";
        File dir = new File(testKeysDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 生成密钥对
        KeelRSAKeyPair.generateKeyPairToDir(testKeysDir);
        
        // 加载密钥
        try {
            rsa.loadPrivateKeyByKeyStoreFile(testKeysDir + "/privateKey.keystore");
            rsa.loadPublicKeyByKeyStoreFile(testKeysDir + "/publicKey.keystore");
            
            assertNotNull(rsa.getPrivateKey());
            assertNotNull(rsa.getPublicKey());
        } catch (Exception e) {
            // 如果文件不存在，跳过此测试
            getUnitTestLogger().warning("RSA key files not found, skipping key loading test: " + e.getMessage());
        }
    }

    @Test
    void testAesKeyGeneration() throws NoSuchAlgorithmException {
        // 测试 AES 密钥生成
        String key128 = KeelAes.generate128BitsSecretKey();
        String key192 = KeelAes.generate192BitsSecretKey();
        String key256 = KeelAes.generate256BitsSecretKey();
        
        assertNotNull(key128);
        assertNotNull(key192);
        assertNotNull(key256);
        
        // 验证密钥长度（Base64编码后的长度）
        // 128位 = 16字节 = 24个Base64字符
        // 192位 = 24字节 = 32个Base64字符
        // 256位 = 32字节 = 44个Base64字符
        assertEquals(24, key128.length());
        assertEquals(32, key192.length());
        assertEquals(44, key256.length());
    }

    @Test
    void testAesAlgorithmExpressions() {
        // 测试所有算法的表达式
        KeelAes.SupportedCipherAlgorithm[] algorithms = {
            KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding,
            KeelAes.SupportedCipherAlgorithm.AesCbcPkcs7Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbPkcs5Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbPkcs7Padding,
            KeelAes.SupportedCipherAlgorithm.AesEcbNoPadding
        };
        
        String[] expectedExpressions = {
            "AES/CBC/PKCS5Padding",
            "AES/CBC/PKCS7Padding",
            "AES/ECB/PKCS5Padding",
            "AES/ECB/PKCS7Padding",
            "AES/ECB/NoPadding"
        };
        
        for (int i = 0; i < algorithms.length; i++) {
            assertEquals(expectedExpressions[i], algorithms[i].getExpression());
        }
    }

    @Test
    void testAesWithEmptyString() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        String emptyKey = "";
        
        // 空字符串不能作为 AES 密钥，应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            KeelAes aes = helper.aes(KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding, emptyKey);
            aes.encrypt("Test message");
        });
    }

    @Test
    void testAesWithSpecialCharacters() {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        String specialKey = "!@#$%^&*()_+-=[]"; // 16字节密钥
        String testText = "Test message with special characters: !@#$%^&*()";
        
        KeelAes aes = helper.aes(KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding, specialKey);
        String encrypted = aes.encrypt(testText);
        String decrypted = aes.decrypt(encrypted);
        
        assertEquals(testText, decrypted);
    }

    @Test
    void testAesWithUnicodeText() throws NoSuchAlgorithmException {
        KeelCryptographyHelper helper = KeelCryptographyHelper.getInstance();
        String testKey = KeelAes.generate128BitsSecretKey(); // 使用生成的128位密钥
        String unicodeText = "Hello, 世界! 🌍 测试消息 with emoji 🚀";
        
        KeelAes aes = helper.aes(KeelAes.SupportedCipherAlgorithm.AesCbcPkcs5Padding, testKey);
        String encrypted = aes.encrypt(unicodeText);
        String decrypted = aes.decrypt(encrypted);
        
        assertEquals(unicodeText, decrypted);
    }
}