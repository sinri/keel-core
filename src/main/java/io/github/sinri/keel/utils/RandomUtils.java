package io.github.sinri.keel.utils;


import io.vertx.ext.auth.prng.VertxContextPRNG;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * As of 4.1.1, fix the static method to use the instance method.
 * @since 3.0.1
 */
public class RandomUtils {
    private static final AtomicReference<VertxContextPRNG> prngRef = new AtomicReference<>();

    private RandomUtils() {
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        VertxContextPRNG prng = getPRNG();

        for (int i = 0; i < length; i++) {
            int index = prng.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 生成指定长度的随机字母数字字符串（只包含字母和数字）
     *
     * @param length 字符串长度
     * @return 随机字母数字字符串
     */
    public static String generateRandomAlphanumericString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        VertxContextPRNG prng = getPRNG();

        for (int i = 0; i < length; i++) {
            int index = prng.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 从列表中随机获取一个元素
     *
     * @param list 元素列表
     * @param <T>  元素类型
     * @return 随机选择的元素
     */
    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null or empty");
        }

        VertxContextPRNG prng = getPRNG();
        int index = prng.nextInt(list.size());
        return list.get(index);
    }

    /**
     * 随机打乱列表元素顺序
     *
     * @param list 原始列表
     * @param <T>  元素类型
     * @return 打乱后的新列表
     */
    public static <T> List<T> shuffleList(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        List<T> shuffled = new ArrayList<>(list);
        VertxContextPRNG prng = getPRNG();

        for (int i = shuffled.size() - 1; i > 0; i--) {
            int index = prng.nextInt(i + 1);
            T temp = shuffled.get(index);
            shuffled.set(index, shuffled.get(i));
            shuffled.set(i, temp);
        }

        return shuffled;
    }

    /**
     * 生成指定范围内的随机整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int generateRandomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min cannot be greater than max");
        }

        VertxContextPRNG prng = getPRNG();
        return min + prng.nextInt(max - min + 1);
    }

    /**
     * 生成指定数量和长度的唯一随机字符串列表
     *
     * @param count  字符串数量
     * @param length 每个字符串的长度
     * @return 唯一随机字符串列表
     */
    public static List<String> generateUniqueRandomStrings(int count, int length) {
        if (count <= 0 || length <= 0) {
            throw new IllegalArgumentException("Count and length must be positive");
        }

        Set<String> uniqueStrings = new HashSet<>();

        while (uniqueStrings.size() < count) {
            uniqueStrings.add(generateRandomString(length));
        }

        return new ArrayList<>(uniqueStrings);
    }

    /**
     * 获取伪随机数生成器实例
     * <p>
     * 使用双重检查锁定模式确保线程安全，支持懒加载。
     * 优先使用 Vertx 上下文，如果 Vertx 未初始化则使用默认上下文。
     *
     * @return 伪随机数生成器实例，永不为 null
     * @throws IllegalStateException 如果无法创建伪随机数生成器
     * @since 3.2.11 build when first get
     */
    @Nonnull
    public static VertxContextPRNG getPRNG() {
        return prngRef.updateAndGet(existing -> {
            if (existing != null) {
                return existing;
            }
            
            try {
                if (Keel.isVertxInitialized()) {
                    return VertxContextPRNG.current(Keel.getVertx());
                } else {
                    return VertxContextPRNG.current();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create VertxContextPRNG instance", e);
            }
        });
    }
}
