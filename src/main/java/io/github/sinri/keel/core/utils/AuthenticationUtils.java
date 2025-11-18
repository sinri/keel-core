package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.authenticator.googleauth.GoogleAuthenticatorConfig;
import io.github.sinri.keel.core.utils.authenticator.googleauth.async.AsyncGoogleAuthenticator;
import io.github.sinri.keel.core.utils.authenticator.googleauth.sync.GoogleAuthenticator;
import io.github.sinri.keel.core.utils.encryption.bcrypt.BCrypt;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * 认证工具类。
 *
 * @since 5.0.0
 */
public class AuthenticationUtils {

    private AuthenticationUtils() {

    }


    /**
     * 使用 BCrypt 算法对密码进行哈希处理。
     * <p>此方法类似于 PHP 的 password_hash 函数，使用 BCrypt 算法生成密码哈希值。
     *
     * @param password 待哈希的原始密码
     * @return 哈希后的密码字符串
     */
    @NotNull
    public static String php_password_hash(@NotNull String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 验证密码是否与哈希值匹配。
     * <p>此方法类似于 PHP 的 password_verify 函数，使用 BCrypt 算法验证密码。
     *
     * @param password 待验证的原始密码
     * @param hash 已存储的密码哈希值
     * @return 如果密码匹配返回 true，否则返回 false
     */
    public static boolean php_password_verify(@NotNull String password, @NotNull String hash) {
        return BCrypt.checkpw(password, hash);
    }

    /**
     * 创建 Google Authenticator 实例，使用默认配置（窗口大小为 1）。
     * <p>此方法创建一个同步版本的 Google Authenticator，用于生成和验证基于时间的一次性密码（TOTP）。
     *
     * @return Google Authenticator 实例
     */
    public static GoogleAuthenticator getGoogleAuthenticator() {
        return getGoogleAuthenticator(configBuilder -> configBuilder.setWindowSize(1));
    }

    /**
     * 创建 Google Authenticator 实例，使用指定的配置。
     * <p>此方法创建一个同步版本的 Google Authenticator，允许通过配置构建器自定义配置参数。
     *
     * @param configBuildHandler 配置构建器处理器，用于自定义配置（可为 null，使用默认配置）
     * @return Google Authenticator 实例
     */
    public static GoogleAuthenticator getGoogleAuthenticator(@Nullable Handler<GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder> configBuildHandler) {
        var configBuilder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        if (configBuildHandler != null) {
            configBuildHandler.handle(configBuilder);
        }
        return new GoogleAuthenticator(configBuilder.build());
    }

    /**
     * 创建异步版本的 Google Authenticator 实例，使用默认配置（窗口大小为 1）。
     * <p>此方法创建一个异步版本的 Google Authenticator，用于生成和验证基于时间的一次性密码（TOTP）。
     *
     * @return 异步 Google Authenticator 实例
     */
    public static AsyncGoogleAuthenticator getAsyncGoogleAuthenticator() {
        return getAsyncGoogleAuthenticator(configBuilder -> configBuilder.setWindowSize(1));
    }

    /**
     * 创建异步版本的 Google Authenticator 实例，使用指定的配置。
     * <p>此方法创建一个异步版本的 Google Authenticator，允许通过配置构建器自定义配置参数。
     *
     * @param configBuildHandler 配置构建器处理器，用于自定义配置（可为 null，使用默认配置）
     * @return 异步 Google Authenticator 实例
     */
    public static AsyncGoogleAuthenticator getAsyncGoogleAuthenticator(@Nullable Handler<GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder> configBuildHandler) {
        var configBuilder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        if (configBuildHandler != null) {
            configBuildHandler.handle(configBuilder);
        }
        return new AsyncGoogleAuthenticator(configBuilder.build());
    }

}
