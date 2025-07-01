package io.github.sinri.keel.facade.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 4.1.0
 */
public class KeelConfigProperty {
    private final List<String> keychain = new ArrayList<>();
    private @Nonnull String value = "";

    public KeelConfigProperty() {
    }

    public final KeelConfigProperty setKeychain(@Nonnull List<String> keychain) {
        this.keychain.addAll(keychain);
        return this;
    }

    public final KeelConfigProperty addToKeychain(@Nonnull String key) {
        this.keychain.add(key);
        return this;
    }

    public final KeelConfigProperty setValue(@Nullable String value) {
        this.value = Objects.requireNonNullElse(value, "");
        return this;
    }

    @Override
    public final String toString() {
        return String.join(".", keychain) + "=" + value;
    }
}
