package io.github.sinri.keel.core.utils.encryption.aes;

import org.jspecify.annotations.NullMarked;

/**
 * @since 5.0.0
 */
@NullMarked
abstract public class KeelAesBase implements KeelAes {
    /**
     * 密钥
     */
    private final String key;

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesBase(String key) {
        this.key = key;
    }

    protected String getKey() {
        return key;
    }
}
