package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.encryption.aes.KeelAes;
import io.github.sinri.keel.core.utils.encryption.rsa.KeelRSA;
import org.jetbrains.annotations.NotNull;

/**
 * 加解密工具类。
 *
 * @since 5.0.0
 */
public class CryptographyUtils {

    private CryptographyUtils() {

    }

    public static KeelAes aes(@NotNull KeelAes.SupportedCipherAlgorithm cipherAlgorithm, @NotNull String key) {
        return KeelAes.create(cipherAlgorithm, key);
    }

    public static KeelRSA rsa() {
        return new KeelRSA();
    }
}
