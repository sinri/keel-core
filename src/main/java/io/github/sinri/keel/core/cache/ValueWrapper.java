package io.github.sinri.keel.core.cache;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;

/**
 * @since 2.5 moved from inner class to here
 * @since 3.2.15 value use SoftReference
 */
public class ValueWrapper<P> {
    private final SoftReference<P> value;
    private final long death;
    private final long birth;

    public ValueWrapper(P value, long lifeInSeconds) {
        this.value = new SoftReference<>(value);
        this.birth = System.currentTimeMillis();//new Date().getTime();
        this.death = this.birth + lifeInSeconds * 1000L;
    }

    public long getBirth() {
        return birth;
    }

    public long getDeath() {
        return death;
    }

    private boolean isInAlivePeriod() {
        long now = System.currentTimeMillis();
        boolean alive = now < this.death && now >= this.birth;
        if (!alive) {
            value.clear();
        }
        return alive;
    }

    @Nullable
    public P getValue() {
        if (isInAlivePeriod()) {
            return value.get();
        } else {
            return null;
        }
    }

    /**
     * @since 4.0.0 clear soft reference of value when died.
     * @deprecated use isAvailable instead.
     */
    @Deprecated(since = "4.0.0", forRemoval = true)
    public boolean isAliveNow() {
        return isAvailable();
    }

    public boolean isAvailable() {
        if (isInAlivePeriod()) {
            return getValue() != null;
        } else {
            return false;
        }
    }
}
