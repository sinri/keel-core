package io.github.sinri.keel.core.helper;


import io.vertx.ext.auth.prng.VertxContextPRNG;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.1
 */
public class KeelRandomHelper {
    private static final KeelRandomHelper instance = new KeelRandomHelper();
    private final AtomicReference<VertxContextPRNG> prngRef;

    private KeelRandomHelper() {
        prngRef = new AtomicReference<>();
    }

    /**
     * 获取 KeelRandomHelper 的单例实例
     *
     * @return KeelRandomHelper 实例
     */
    public static KeelRandomHelper getInstance() {
        return instance;
    }

    /**
     * @return Pseudo Random Number Generator
     * @since 3.2.11 build when first get
     */
    @Nonnull
    public VertxContextPRNG getPRNG() {
        if (prngRef.get() == null) {
            synchronized (prngRef) {
                if (prngRef.get() == null) {
                    if (Keel.isVertxInitialized()) {
                        prngRef.set(VertxContextPRNG.current(Keel.getVertx()));
                    } else {
                        prngRef.set(VertxContextPRNG.current());
                    }
                }
            }
        }
        VertxContextPRNG prng = prngRef.get();
        Objects.requireNonNull(prng);
        return prng;
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
        VertxContextPRNG prng = instance.getPRNG();
        
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
        VertxContextPRNG prng = instance.getPRNG();
        
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
     * @param <T> 元素类型
     * @return 随机选择的元素
     */
    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null or empty");
        }
        
        VertxContextPRNG prng = instance.getPRNG();
        int index = prng.nextInt(list.size());
        return list.get(index);
    }
    
    /**
     * 随机打乱列表元素顺序
     *
     * @param list 原始列表
     * @param <T> 元素类型
     * @return 打乱后的新列表
     */
    public static <T> List<T> shuffleList(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null");
        }
        
        List<T> shuffled = new ArrayList<>(list);
        VertxContextPRNG prng = instance.getPRNG();
        
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
        
        VertxContextPRNG prng = instance.getPRNG();
        return min + prng.nextInt(max - min + 1);
    }
    
    /**
     * 生成指定数量和长度的唯一随机字符串列表
     *
     * @param count 字符串数量
     * @param length 每个字符串的长度
     * @return 唯一随机字符串列表
     */
    public static List<String> generateUniqueRandomStrings(int count, int length) {
        if (count <= 0 || length <= 0) {
            throw new IllegalArgumentException("Count and length must be positive");
        }
        
        Set<String> uniqueStrings = new HashSet<>();
        VertxContextPRNG prng = instance.getPRNG();
        
        while (uniqueStrings.size() < count) {
            uniqueStrings.add(generateRandomString(length));
        }
        
        return new ArrayList<>(uniqueStrings);
    }
}
