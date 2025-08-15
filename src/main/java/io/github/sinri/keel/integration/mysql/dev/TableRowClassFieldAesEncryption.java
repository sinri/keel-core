package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.encryption.aes.AESValueEnvelope;

import javax.annotation.Nonnull;

/**
 * Use with {@link AESValueEnvelope}, bind a field to a certain AES Value Envelope.
 *
 * @since 3.1.0 Technical Preview
 * @deprecated As of 4.1.1, use {@link TableRowClassFieldAnyEnvelope} instead.
 */
@TechnicalPreview(since = "3.1.0")
@Deprecated(since = "4.1.1")
public class TableRowClassFieldAesEncryption extends TableRowClassFieldAnyEnvelope {
    public TableRowClassFieldAesEncryption(@Nonnull String envelopeName, @Nonnull String envelopePackage) {
        super(envelopeName, envelopePackage);
    }
}
