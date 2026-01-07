package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;


/**
 * SMTP协议工具。
 *
 * @since 5.0.0
 * @deprecated 基于 {@link SmtpConfigElement#toMailConfig()} 方法，可以直接创建使用 {@link MailClient} 实例。
 */
@Deprecated(since = "5.0.0")
@NullMarked
public class KeelSmtpKit {

    private final MailConfig mailConfig;
    private final MailClient mailClient;


    public KeelSmtpKit(Vertx vertx, MailConfig mailConfig, @Nullable String poolName) {
        this.mailConfig = mailConfig;
        if (poolName != null) {
            this.mailClient = MailClient.createShared(vertx, this.mailConfig, poolName);
        } else {
            this.mailClient = MailClient.create(vertx, this.mailConfig);
        }
    }

    public KeelSmtpKit(Vertx vertx, String smtpName, boolean shared) throws NotConfiguredException {
        this(vertx, buildMailConfig(smtpName), shared ? Objects.requireNonNull(smtpName) : null);
    }

    public KeelSmtpKit(Vertx vertx, String smtpName) throws NotConfiguredException {
        this(vertx, smtpName, true);
    }

    private static MailConfig buildMailConfig(String smtpName) throws NotConfiguredException {
        var smtpConfiguration = ConfigElement.root().extract(List.of("email", "smtp", smtpName));
        Objects.requireNonNull(smtpConfiguration);
        SmtpConfigElement smtpConfigElement = new SmtpConfigElement(smtpConfiguration);
        return smtpConfigElement.toMailConfig();
    }

    public MailClient getMailClient() {
        return mailClient;
    }

    public Future<Void> close() {
        return mailClient.close();
    }

    public Future<MailResult> quickSendTextMail(
            List<String> receivers,
            String subject,
            String textContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setText(textContent);

        return this.mailClient.sendMail(message);
    }

    public Future<MailResult> quickSendHtmlMail(
            List<String> receivers,
            String subject,
            String htmlContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setHtml(htmlContent);

        return this.mailClient.sendMail(message);
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
