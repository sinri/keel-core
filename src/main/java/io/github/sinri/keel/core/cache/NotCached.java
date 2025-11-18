package io.github.sinri.keel.core.cache;

import org.jetbrains.annotations.NotNull;

/**
 * 对应键未缓存。
 *
 * @since 5.0.0
 */
public class NotCached extends Exception {
    public NotCached(@NotNull String key) {
        super("For key [" + key + "], no available cached record found.");
    }
}
