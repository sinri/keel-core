package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.ext.mail.MailConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @since 4.1.0
 */
public class KeelSmtpConfigElement extends ConfigElement {
    public KeelSmtpConfigElement(@NotNull String name) {
        super(name);
    }

    public KeelSmtpConfigElement(@NotNull ConfigElement another) {
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
