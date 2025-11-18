package io.github.sinri.keel.core.integration.email.smtp;

import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * SMTP协议工具。
 *
 * @since 5.0.0
 */
public class KeelSmtpKit {
    private final MailConfig mailConfig;
    private final MailClient mailClient;


    public KeelSmtpKit(@NotNull MailConfig mailConfig, @Nullable String poolName) {
        this.mailConfig = mailConfig;
        if (poolName != null) {
            this.mailClient = MailClient.createShared(Keel.getVertx(), this.mailConfig, poolName);
        } else {
            this.mailClient = MailClient.create(Keel.getVertx(), this.mailConfig);
        }
    }

    public KeelSmtpKit(@NotNull String smtpName, boolean shared) {
        this(buildMailConfig(smtpName), shared ? Objects.requireNonNull(smtpName) : null);
    }

    public KeelSmtpKit(@NotNull String smtpName) {
        this(smtpName, true);
    }

    public KeelSmtpKit() {
        this(Objects.requireNonNull(
                Keel.config("email.smtp.default_smtp_name"),
                "email.smtp.default_smtp_name is not configured"
        ));
    }

    private static MailConfig buildMailConfig(@NotNull String smtpName) {
        var smtpConfiguration = Keel.getConfiguration().extract("email", "smtp", smtpName);
        Objects.requireNonNull(smtpConfiguration);

        SmtpConfigElement smtpConfigElement = new SmtpConfigElement(smtpConfiguration);
        return smtpConfigElement.toMailConfig();
    }

    public MailClient getMailClient() {
        return mailClient;
    }

    public Future<Void> close() {
        if (null != mailClient) {
            return mailClient.close();
        }
        return Future.succeededFuture();
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

    /**
     * @since 3.0.6
     */
    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
