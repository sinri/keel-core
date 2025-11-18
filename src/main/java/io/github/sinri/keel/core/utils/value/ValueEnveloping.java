package io.github.sinri.keel.core.utils.value;


import org.jetbrains.annotations.Nullable;

/**
 * 本接口定义一个基于特定方法对值进行密封和解封的工具。
 *
 * @param <R> 原始的值类型
 * @param <E> 密封后的值类型
 * @since 5.0.0
 */
public interface ValueEnveloping<R, E> {
    @Nullable
    E encrypt(@Nullable R raw);

    @Nullable
    R decrypt(@Nullable E decrypted);
}
