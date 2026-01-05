package io.github.sinri.keel.core.utils.encryption.aes;

import org.jspecify.annotations.NullMarked;

/**
 * @since 5.0.0
 */
@NullMarked
abstract public class KeelAesUsingPkcs5Padding extends KeelAesBase {
    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesUsingPkcs5Padding(String key) {
        super(key);
    }
}
