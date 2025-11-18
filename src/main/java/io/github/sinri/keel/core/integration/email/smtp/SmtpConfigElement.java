package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.ext.mail.MailConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * 面向SMTP协议工具所需的配置的配置节点，可转化为{@link MailConfig}实例。
 *
 * @since 5.0.0
 */
public class SmtpConfigElement extends ConfigElement {
    public SmtpConfigElement(@NotNull String name) {
        super(name);
    }

    public SmtpConfigElement(@NotNull ConfigElement another) {
        super(another);
    }

    public MailConfig toMailConfig() {
        var mailConfig = new MailConfig();
        mailConfig.setHostname(getHostname());
        mailConfig.setPort(getPort());
        mailConfig.setUsername(getUsername());
        mailConfig.setPassword(getPassword());
        mailConfig.setSsl(isSSL());

        return mailConfig;
    }

    public String getHostname() {
        return readString(List.of("hostname"), null);
    }

    public Integer getPort() {
        return readInteger(List.of("port"), 25);
    }

    public String getUsername() {
        return readString(List.of("username"), null);
    }

    public String getPassword() {
        return readString(List.of("password"), null);
    }

    public boolean isSSL() {
        return readBoolean(List.of("ssl"), false);
    }
}
