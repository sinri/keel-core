package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.encryption.aes.KeelAes;
import io.github.sinri.keel.core.utils.encryption.rsa.KeelRSA;
import org.jspecify.annotations.NullMarked;

/**
 * 加解密工具类。
 *
 * @since 5.0.0
 */
@NullMarked
public class CryptographyUtils {

    private CryptographyUtils() {

    }

    public static KeelAes aes(KeelAes.SupportedCipherAlgorithm cipherAlgorithm, String key) {
        return KeelAes.create(cipherAlgorithm, key);
    }

    public static KeelRSA rsa() {
        return new KeelRSA();
    }
}
