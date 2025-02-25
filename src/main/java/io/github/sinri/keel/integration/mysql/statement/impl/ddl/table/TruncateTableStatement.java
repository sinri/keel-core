package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html">TRUNCATE TABLE Statement</a>
 * @since 4.0.4
 */
public class TruncateTableStatement extends AbstractStatement {
    private @Nullable String schemaName = null;
    private @Nonnull String tableName = "";

    @Nullable
    protected String getSchemaName() {
        return schemaName;
    }

    public TruncateTableStatement setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    @Nonnull
    protected String getTableName() {
        return tableName;
    }

    public TruncateTableStatement setTableName(@Nonnull String tableName) {
        this.tableName = tableName;
        return this;
    }

    protected final String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`.")) + ("`" + tableName + "`");
    }

    @Override
    public String toString() {
        return "TRUNCATE TABLE " + getTableExpression();
    }
}
