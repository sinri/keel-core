package io.github.sinri.keel.utils;

import io.github.sinri.keel.utils.authenticator.googleauth.GoogleAuthenticatorConfig;
import io.github.sinri.keel.utils.authenticator.googleauth.async.AsyncGoogleAuthenticator;
import io.github.sinri.keel.utils.authenticator.googleauth.sync.GoogleAuthenticator;
import io.github.sinri.keel.utils.encryption.bcrypt.BCrypt;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 2.9.4
 */
public class AuthenticationUtils {

    private AuthenticationUtils() {

    }


    /**
     * @since 2.8
     * @since 2.9.4 moved from digest to authentication
     */
    public static @Nonnull String php_password_hash(@Nonnull String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * @since 2.8
     * @since 2.9.4 moved from digest to authentication
     */
    public static boolean php_password_verify(@Nonnull String password, @Nonnull String hash) {
        return BCrypt.checkpw(password, hash);
    }

    /**
     * To create an instance of Google Authenticator with default config of window size 1.
     *
     * @since 2.9.4
     */
    public static GoogleAuthenticator getGoogleAuthenticator() {
        return getGoogleAuthenticator(configBuilder -> configBuilder.setWindowSize(1));
    }

    /**
     * To create an instance of Google Authenticator with certain config,
     *
     * @since 2.9.4
     */
    public static GoogleAuthenticator getGoogleAuthenticator(@Nullable Handler<GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder> configBuildHandler) {
        var configBuilder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        if (configBuildHandler != null) {
            configBuildHandler.handle(configBuilder);
        }
        return new GoogleAuthenticator(configBuilder.build());
    }

    /**
     * To create an instance of Google Authenticator with default config of window size 1.
     *
     * @since 3.2.9
     */
    public static AsyncGoogleAuthenticator getAsyncGoogleAuthenticator() {
        return getAsyncGoogleAuthenticator(configBuilder -> configBuilder.setWindowSize(1));
    }

    /**
     * To create an instance of Google Authenticator with certain config,
     *
     * @since 3.2.9
     */
    public static AsyncGoogleAuthenticator getAsyncGoogleAuthenticator(@Nullable Handler<GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder> configBuildHandler) {
        var configBuilder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        if (configBuildHandler != null) {
            configBuildHandler.handle(configBuilder);
        }
        return new AsyncGoogleAuthenticator(configBuilder.build());
    }

//    public String createSecretForTOTP(GoogleAuthenticator googleAuthenticator) {
//        return googleAuthenticator.createCredentials().getKey();
//    }
//
//    public int generateTOTP(GoogleAuthenticator googleAuthenticator, String shared_secret) {
//        return googleAuthenticator.getTotpPassword(shared_secret);
//    }
//
//    public int generateTOTP(GoogleAuthenticator googleAuthenticator, String shared_secret, long time) {
//        return googleAuthenticator.getTotpPassword(shared_secret, time);
//    }
//
//    public boolean validate(GoogleAuthenticator googleAuthenticator, String shared_secret, int totp) {
//        return googleAuthenticator.authorize(shared_secret, totp);
//    }

}
