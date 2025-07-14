package io.github.sinri.keel.integration.email.smtp;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.vertx.ext.mail.MailConfig;

import javax.annotation.Nonnull;

/**
 * @since 4.1.0
 */
public class KeelSmtpConfigElement extends KeelConfigElement {
    public KeelSmtpConfigElement(@Nonnull String name) {
        super(name);
    }

    public KeelSmtpConfigElement(@Nonnull KeelConfigElement another) {
        super(another);
    }

    public MailConfig toMailConfig() {
        // var smtpConfiguration = Keel.getConfiguration().extract("email", "smtp", smtpName);
        // Objects.requireNonNull(smtpConfiguration);

        var mailConfig = new MailConfig();
        mailConfig.setHostname(getHostname());
        mailConfig.setPort(getPort());
        mailConfig.setUsername(getUsername());
        mailConfig.setPassword(getPassword());
        mailConfig.setSsl(isSSL());

        return mailConfig;
    }

    public String getHostname() {
        return readString("hostname", null);
    }

    public Integer getPort() {
        return readInteger("port", 25);
    }

    public String getUsername() {
        return readString("username", null);
    }

    public String getPassword() {
        return readString("password", null);
    }

    public boolean isSSL() {
        return readBoolean("ssl", false);
    }
}
