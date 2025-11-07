package io.github.sinri.keel.utils.encryption.aes;

import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;

/**
 * PKCS7Padding is not supported in Java Native Environment,
 * you need to add BouncyCastleProvider to support it.
 * <p>
 * To use this class, add the following BouncyCastle dependency to your pom.xml:
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.bouncycastle&lt;/groupId&gt;
 *     &lt;artifactId&gt;bcprov-jdk15on&lt;/artifactId&gt;
 *     &lt;version&gt;1.70&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 * <p>
 * Before using this class, make sure to initialize the BouncyCastle provider by calling:
 * {@code KeelAesUsingPkcs7Padding.requireBouncyCastleProvider()}
 * <p>
 * This should be done once during application startup.
 *
 * @since 2.8
 */
abstract public class KeelAesUsingPkcs7Padding extends KeelAesBase {
//    static {
//        //如果是PKCS7Padding填充方式，则必须加上下面这行
//        Security.addProvider(new BouncyCastleProvider());
//    }

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesUsingPkcs7Padding(String key) {
        super(key);
    }

    /**
     * 直接使用static代码块引用 org.bouncycastle.jce.provider.BouncyCastleProvider 会引起shade问题：
     * `Invalid signature file digest for Manifest main attributes`。
     * 当需要使用BC支持时，应当在POM引入相应的依赖，在MAIN的初始化中提前调用此方法
     */
    public static void requireBouncyCastleProvider() {
        /*
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15on</artifactId>
            <version>1.70</version>
        </dependency>
         */

        try {
            Class<?> providerClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Provider provider = (Provider) providerClass.getConstructor().newInstance();
            Security.addProvider(provider);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
