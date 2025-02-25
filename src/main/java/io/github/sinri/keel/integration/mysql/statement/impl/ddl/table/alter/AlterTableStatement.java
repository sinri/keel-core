package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/alter-table.html">ALTER TABLE Statement</a>
 * @since 4.0.4
 */
public class AlterTableStatement extends AbstractStatement {
    private final List<TableAlterOption> alterOptions = new ArrayList<>();
    private @Nullable String schemaName = null;
    private @Nonnull String tableName = "";
    private @Nullable TableAlterPartitionOptions partitionOptions = null;

    public AlterTableStatement setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public AlterTableStatement setTableName(@Nonnull String tableName) {
        this.tableName = tableName;
        return this;
    }

    public AlterTableStatement setPartitionOptions(@Nullable TableAlterPartitionOptions partitionOptions) {
        this.partitionOptions = partitionOptions;
        return this;
    }

    protected String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`")) + ".`" + tableName + "`";
    }

    @Override
    public String toString() {
        // ALTER TABLE tbl_name
        //    [alter_option [, alter_option] ...]
        //    [partition_options]
        return "ALTER TABLE " + getTableExpression() + " " + SQL_COMPONENT_SEPARATOR
                + Keel.stringHelper().joinStringArray(alterOptions, ",") + " " + SQL_COMPONENT_SEPARATOR
                + (partitionOptions == null ? "" : partitionOptions);
    }

}
