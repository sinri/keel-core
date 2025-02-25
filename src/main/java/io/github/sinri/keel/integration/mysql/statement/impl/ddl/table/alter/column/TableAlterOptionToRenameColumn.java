package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;

import javax.annotation.Nonnull;

/**
 * {@code RENAME COLUMN old_col_name TO new_col_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToRenameColumn extends TableAlterOption {
    public @Nonnull String columnName = "";
    public @Nonnull String newColumnName = "";


    public TableAlterOptionToRenameColumn setColumnName(@Nonnull String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableAlterOptionToRenameColumn setNewColumnName(@Nonnull String newColumnName) {
        this.newColumnName = newColumnName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME COLUMN `" + columnName + "` TO `" + newColumnName + "`";
    }
}
