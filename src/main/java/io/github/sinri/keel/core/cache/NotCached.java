package io.github.sinri.keel.core.cache;

import org.jspecify.annotations.NullMarked;

/**
 * 对应键未缓存。
 *
 * @since 5.0.0
 */
@NullMarked
public class NotCached extends Exception {
    public NotCached(String key) {
        super("For key [" + key + "], no available cached record found.");
    }
}
