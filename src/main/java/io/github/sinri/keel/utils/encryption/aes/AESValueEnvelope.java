package io.github.sinri.keel.utils.encryption.aes;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.core.KeelValueEnvelope;
import org.jetbrains.annotations.Nullable;


/**
 * @since 3.1.0 Technical Preview
 */
@TechnicalPreview(since = "3.1.0")
public interface AESValueEnvelope extends KeelValueEnvelope {
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
