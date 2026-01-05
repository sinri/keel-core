package io.github.sinri.keel.core.utils.value;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 三态存值器。
 * <p>
 * 三态存值器的存值状态有：
 * <ul>
 *     <li>未设置或已过期</li>
 *     <li>已设置为空值</li>
 *     <li>已设置为非空值</li>
 * </ul>
 *
 * @param <T> 值类型
 * @since 5.0.0
 */
@NullMarked
public class ValueBox<T extends @Nullable Object> {
    private volatile T value;
    private volatile boolean valueAlreadySet;
    /**
     * 过期时间。
     * <p>
     * 值为正值时，通过与{@link System#currentTimeMillis()}比较来判断值是否过期；
     * 否则视为永不过期。
     */
    private volatile long expire = 0;

    /**
     * 构造一个新的 ValueBox 实例并清空其内部状态。
     * <p>
     * 此构造函数初始化一个空的 ValueBox，将内部状态设置为未设置值，并重置任何过期时间。
     */
    public ValueBox() {
        this.clear();
    }

    /**
     * 使用指定值构造一个新的 ValueBox 实例，不设置过期时间。
     *
     * @param value 要存储在盒子中的初始值，可以为 null
     */
    public ValueBox(@Nullable T value) {
        this(value, 0);
    }

    /**
     * 使用指定值和可选的过期时间构造一个新的 ValueBox 实例。
     *
     * @param value    要存储在盒子中的初始值，可以为 null
     * @param lifetime 值的生命周期（毫秒），超过此时间后值将过期并被清除；
     *                 如果为 0 或负数，值将永不过期
     */
    public ValueBox(@Nullable T value, long lifetime) {
        this.setValue(value, lifetime);
    }

    /**
     * 清空 ValueBox 的内部状态。
     * <p>
     * 将值设置为 null，标记为未设置状态，并清除过期时间。
     */
    public synchronized void clear() {
        this.value = null;
        this.valueAlreadySet = false;
        this.expire = 0;
    }

    /**
     * 检查值是否已设置且未过期。
     * <p>
     * 如果值已设置，且声明了生命周期，则检查是否过期。
     * 在检查时，如果值已设置但已过期，将被自动清除。
     *
     * @return 如果值已设置且未过期返回 true，否则返回 false
     */
    public synchronized boolean isValueAlreadySet() {
        if (!valueAlreadySet)
            return false;
        if (expire <= 0) {
            return true;
        }
        if (expire > System.currentTimeMillis()) {
            return true;
        }
        this.clear();
        return false;
    }

    /**
     * 获取存储在 ValueBox 中的值（如果已设置且未过期）。
     *
     * @return 类型为 T 的值，如果已设置且未过期；否则抛出 {@link IllegalStateException}
     * @throws IllegalStateException 如果值未设置或已过期
     */
    public synchronized @Nullable T getValue() {
        if (isValueAlreadySet())
            return value;
        else
            throw new IllegalStateException("Value is not set yet");
    }

    /**
     * 在 ValueBox 中设置值，不设置过期时间。
     *
     * @param value 要存储在盒子中的值，可以为 null
     * @return 当前 ValueBox 实例，用于方法链式调用
     */
    public synchronized ValueBox<T> setValue(@Nullable T value) {
        return this.setValue(value, 0);
    }

    /**
     * 获取存储在 ValueBox 中的非空值。
     * <p>
     * 如果值未设置、已过期或为 null，将抛出异常。
     *
     * @return 类型为 T 的非空值
     * @throws IllegalStateException 如果值未设置或已过期
     * @throws NullPointerException  如果值已设置但为 null
     * @since 4.1.0
     */
    public synchronized T getNonNullValue() {
        T t = getValue();
        if (t == null) {
            throw new NullPointerException("Value is expected as non-null value");
        }
        return t;
    }

    /**
     * 获取存储在 ValueBox 中的值（如果已设置且未过期）。
     * <p>
     * 如果值未设置或已过期，返回提供的后备值。
     *
     * @param fallbackForInvalid 当存储的值未设置或已过期时返回的后备值
     * @return 如果值已设置且未过期则返回该值，否则返回后备值
     * @since 3.1.0
     */
    public synchronized @Nullable T getValueOrElse(@Nullable T fallbackForInvalid) {
        if (isValueAlreadySet())
            return value;
        else
            return fallbackForInvalid;
    }

    public synchronized T ensureNonNullValue(Supplier<EnsuredValueWithExpire<@NonNull T>> supplier) {
        if (isValueSetAndNotNull())
            return value;
        EnsuredValueWithExpire<@NonNull T> pair = supplier.get();
        value = pair.value;
        expire = pair.expire;
        return value;
    }

    /**
     * 在 ValueBox 中设置值，并可选择设置过期时间。
     *
     * @param value    要存储在盒子中的值，可以为 null
     * @param lifetime 值的生命周期（毫秒），超过此时间后值将过期并被清除；
     *                 如果为 0 或负数，值将永不过期
     * @return 当前 ValueBox 实例，用于方法链式调用
     */
    public synchronized ValueBox<T> setValue(@Nullable T value, long lifetime) {
        this.value = value;
        this.valueAlreadySet = true;
        if (lifetime > 0) {
            this.expire = System.currentTimeMillis() + lifetime;
        } else {
            this.expire = 0;
        }
        return this;
    }

    /**
     * 检查 ValueBox 中的值是否已设置为 null。
     *
     * @return 如果值已设置且为 null 返回 true，否则返回 false
     */
    public synchronized boolean isValueSetToNull() {
        return this.isValueAlreadySet() && this.getValue() == null;
    }

    /**
     * 检查 ValueBox 中的值是否已设置且不为 null。
     *
     * @return 如果值已设置且不为 null 返回 true，否则返回 false
     */
    public synchronized boolean isValueSetAndNotNull() {
        return this.isValueAlreadySet() && this.getValue() != null;
    }

    @NullMarked
    public record EnsuredValueWithExpire<E>(E value, long expire) {
    }
}
