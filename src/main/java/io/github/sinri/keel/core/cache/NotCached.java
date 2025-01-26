package io.github.sinri.keel.core.cache;

public class NotCached extends Exception {
    public NotCached(String key) {
        super("For key [" + key + "], no available cached record found.");
    }
}
