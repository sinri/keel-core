package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.ext.mail.MailConfig;
import org.jspecify.annotations.NullMarked;

import java.util.List;


/**
 * 面向SMTP协议工具所需的配置的配置节点，可转化为{@link MailConfig}实例。
 *
 * @since 5.0.0
 */
@NullMarked
public class SmtpConfigElement extends ConfigElement {


    public SmtpConfigElement(ConfigElement another) {
        super(another);
    }

    public MailConfig toMailConfig() throws NotConfiguredException {
        var mailConfig = new MailConfig();
        mailConfig.setHostname(getHostname());
        mailConfig.setPort(getPort());
        mailConfig.setUsername(getUsername());
        mailConfig.setPassword(getPassword());
        mailConfig.setSsl(isSSL());

        return mailConfig;
    }

    public String getHostname() throws NotConfiguredException {
        return readString(List.of("hostname"));
    }

    public int getPort() {
        try {
            return readInteger(List.of("port"));
        } catch (NotConfiguredException e) {
            return 25;
        }
    }

    public String getUsername() throws NotConfiguredException {
        return readString(List.of("username"));
    }

    public String getPassword() throws NotConfiguredException {
        return readString(List.of("password"));
    }

    public boolean isSSL() {
        try {
            return readBoolean(List.of("ssl"));
        } catch (NotConfiguredException e) {
            return false;
        }
    }
}
