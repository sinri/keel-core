package io.github.sinri.keel.core.utils.encryption.aes;

import io.github.sinri.keel.core.utils.value.ValueEnveloping;
import org.jetbrains.annotations.Nullable;

/**
 * 运用 AES 加密算法，对字符串进行密封和解封。
 * @since 5.0.0
 */
public interface AESValueEnveloping extends ValueEnveloping<String, String> {
    /**
     * Encrypt the raw string and store the encrypted value with this instance.
     *
     * @param raw The raw string value.
     */
    @Nullable
    String encrypt(@Nullable String raw);

    /**
     * Decrypt the stored value and return the raw string.
     */
    @Nullable
    String decrypt(@Nullable String decrypted);
}
