package io.github.sinri.keel.integration.mysql.dev;

import javax.annotation.Nullable;

/**
 * @since 4.1.0
 */
public class TableRowClassBuildStandard {
    private boolean provideConstSchema;
    private boolean provideConstTable;
    private boolean provideConstSchemaAndTable;
    private boolean vcsFriendly;
    private @Nullable String strictEnumPackage;
    private @Nullable String envelopePackage;

    public TableRowClassBuildStandard() {
        provideConstSchema = true;
        provideConstTable = true;
        provideConstSchemaAndTable = false;
        vcsFriendly = false;
        strictEnumPackage = null;
        envelopePackage = null;
    }

    public TableRowClassBuildStandard(TableRowClassBuildStandard another) {
        this.provideConstSchema = another.provideConstSchema;
        this.provideConstTable = another.provideConstTable;
        this.provideConstSchemaAndTable = another.provideConstSchemaAndTable;
        this.vcsFriendly = another.vcsFriendly;
        this.strictEnumPackage = another.strictEnumPackage;
        this.envelopePackage = another.envelopePackage;
    }

    public boolean isVcsFriendly() {
        return vcsFriendly;
    }

    public TableRowClassBuildStandard setVcsFriendly(boolean vcsFriendly) {
        this.vcsFriendly = vcsFriendly;
        return this;
    }

    public boolean isProvideConstSchemaAndTable() {
        return provideConstSchemaAndTable;
    }

    public TableRowClassBuildStandard setProvideConstSchemaAndTable(boolean provideConstSchemaAndTable) {
        this.provideConstSchemaAndTable = provideConstSchemaAndTable;
        return this;
    }

    public boolean isProvideConstTable() {
        return provideConstTable;
    }

    public TableRowClassBuildStandard setProvideConstTable(boolean provideConstTable) {
        this.provideConstTable = provideConstTable;
        return this;
    }

    public boolean isProvideConstSchema() {
        return provideConstSchema;
    }

    public TableRowClassBuildStandard setProvideConstSchema(boolean provideConstSchema) {
        this.provideConstSchema = provideConstSchema;
        return this;
    }

    @Nullable
    public String getEnvelopePackage() {
        return envelopePackage;
    }

    /**
     * @param envelopePackage empty or a package path. No dot in tail.
     */
    public TableRowClassBuildStandard setEnvelopePackage(@Nullable String envelopePackage) {
        this.envelopePackage = envelopePackage;
        return this;
    }

    @Nullable
    public String getStrictEnumPackage() {
        return strictEnumPackage;
    }

    /**
     * @param strictEnumPackage empty or a package path. No dot in tail.
     */
    public TableRowClassBuildStandard setStrictEnumPackage(@Nullable String strictEnumPackage) {
        this.strictEnumPackage = strictEnumPackage;
        return this;
    }
}
