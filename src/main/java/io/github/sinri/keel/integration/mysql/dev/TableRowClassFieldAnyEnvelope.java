package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;

@TechnicalPreview(since = "4.1.1")
public class TableRowClassFieldAnyEnvelope {
    private final String envelopePackage;
    private final String envelopeName;

    public TableRowClassFieldAnyEnvelope(@Nonnull String envelopeName, @Nonnull String envelopePackage) {
        this.envelopePackage = envelopePackage;
        this.envelopeName = envelopeName;
    }

    public String buildCallClassMethodCode(@Nonnull String parameter) {
        return "new " + envelopePackage + "." + envelopeName + "().decrypt(" + parameter + ");";
    }
}
