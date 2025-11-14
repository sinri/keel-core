package io.github.sinri.keel.core;


import org.jetbrains.annotations.Nullable;

/**
 * @since 4.1.1
 */
public interface KeelValueEnvelope {
    @Nullable
    String encrypt(@Nullable String raw);

    @Nullable
    String decrypt(@Nullable String decrypted);
}
