package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;

import javax.annotation.Nonnull;

/**
 * {@code RENAME [TO | AS] new_tbl_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToRenameTable extends TableAlterOption {
    private @Nonnull String newTableName = "";

    public TableAlterOptionToRenameTable setNewTableName(@Nonnull String newTableName) {
        this.newTableName = newTableName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME TO `" + newTableName + "`";
    }
}
