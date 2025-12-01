package io.github.sinri.keel.core.integration.email.smtp;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;


/**
 * SMTP协议工具。
 *
 * @since 5.0.0
 */
public class KeelSmtpKit {
    @NotNull
    private final MailConfig mailConfig;
    @NotNull
    private final MailClient mailClient;


    public KeelSmtpKit(@NotNull Keel keel, @NotNull MailConfig mailConfig, @Nullable String poolName) {
        this.mailConfig = mailConfig;
        if (poolName != null) {
            this.mailClient = MailClient.createShared(keel.getVertx(), this.mailConfig, poolName);
        } else {
            this.mailClient = MailClient.create(keel.getVertx(), this.mailConfig);
        }
    }

    public KeelSmtpKit(@NotNull Keel keel, @NotNull String smtpName, boolean shared) {
        this(keel, buildMailConfig(keel, smtpName), shared ? Objects.requireNonNull(smtpName) : null);
    }

    public KeelSmtpKit(@NotNull Keel keel, @NotNull String smtpName) {
        this(keel, smtpName, true);
    }

    @NotNull
    private static MailConfig buildMailConfig(@NotNull Keel keel, @NotNull String smtpName) {
        var smtpConfiguration = keel.getConfiguration().extract("email", "smtp", smtpName);
        Objects.requireNonNull(smtpConfiguration);

        SmtpConfigElement smtpConfigElement = new SmtpConfigElement(smtpConfiguration);
        return smtpConfigElement.toMailConfig();
    }

    @NotNull
    public MailClient getMailClient() {
        return mailClient;
    }

    @NotNull
    public Future<Void> close() {
        return mailClient.close();
    }

    @NotNull
    public Future<MailResult> quickSendTextMail(
            @NotNull List<String> receivers,
            @NotNull String subject,
            @NotNull String textContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setText(textContent);

        return this.mailClient.sendMail(message);
    }

    @NotNull
    public Future<MailResult> quickSendHtmlMail(
            @NotNull List<String> receivers,
            @NotNull String subject,
            @NotNull String htmlContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setHtml(htmlContent);

        return this.mailClient.sendMail(message);
    }

    @NotNull
    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
