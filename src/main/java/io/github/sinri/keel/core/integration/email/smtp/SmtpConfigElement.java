package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.vertx.ext.mail.MailConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * 面向SMTP协议工具所需的配置的配置节点，可转化为{@link MailConfig}实例。
 *
 * @since 5.0.0
 */
public class SmtpConfigElement extends ConfigTree {

    public SmtpConfigElement(@NotNull ConfigElement another) {
        super(another);
    }

    @NotNull
    public MailConfig toMailConfig() {
        var mailConfig = new MailConfig();
        mailConfig.setHostname(getHostname());
        mailConfig.setPort(getPort());
        mailConfig.setUsername(getUsername());
        mailConfig.setPassword(getPassword());
        mailConfig.setSsl(isSSL());

        return mailConfig;
    }

    @Nullable
    public String getHostname() {
        try {
            return readString(List.of("hostname"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    public int getPort() {
        try {
            return readInteger(List.of("port"));
        } catch (NotConfiguredException e) {
            return 25;
        }
    }

    @Nullable
    public String getUsername() {
        try {
            return readString(List.of("username"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    @Nullable
    public String getPassword() {
        try {
            return readString(List.of("password"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    public boolean isSSL() {
        try {
            return readBoolean(List.of("ssl"));
        } catch (NotConfiguredException e) {
            return false;
        }
    }
}
