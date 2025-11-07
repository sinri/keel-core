package io.github.sinri.keel.utils;

import io.github.sinri.keel.utils.encryption.aes.KeelAes;
import io.github.sinri.keel.utils.encryption.rsa.KeelRSA;

/**
 * @since 2.8
 */
public class CryptographyUtils {

    private CryptographyUtils() {

    }

    public static KeelAes aes(KeelAes.SupportedCipherAlgorithm cipherAlgorithm, String key) {
        return KeelAes.create(cipherAlgorithm, key);
    }

    /**
     * @since 3.0.1
     */
    public static KeelRSA rsa() {
        return new KeelRSA();
    }
}
