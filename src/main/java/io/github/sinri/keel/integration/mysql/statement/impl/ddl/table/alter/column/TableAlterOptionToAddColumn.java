package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@code ADD [COLUMN] col_name column_definition [FIRST | AFTER col_name]}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToAddColumn extends TableAlterOption {
    public @Nonnull String columnName = "";
    public @Nonnull String columnDefinition = "";
    public @Nullable String position;

    public TableAlterOptionToAddColumn setColumnName(@Nonnull String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableAlterOptionToAddColumn setColumnDefinition(@Nonnull String columnDefinition) {
        this.columnDefinition = columnDefinition;
        return this;
    }

    public TableAlterOptionToAddColumn placeToFirst() {
        this.position = "FIRST";
        return this;
    }

    public TableAlterOptionToAddColumn placeAfter(@Nonnull String previousColumnName) {
        this.position = "AFTER `" + previousColumnName + "`";
        return this;
    }

    @Override
    public String toString() {
        return "ADD COLUMN `" + columnName + "` " + columnDefinition + " "
                + (position == null ? "" : position);
    }
}
