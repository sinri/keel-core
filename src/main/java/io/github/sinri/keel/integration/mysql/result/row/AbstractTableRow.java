package io.github.sinri.keel.integration.mysql.result.row;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * As of 2.8 rename and remove.
 *
 * @since 2.0
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(@Nonnull JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * @return default null
     */
    @Nullable
    public String sourceSchemaName() {
        return null;
    }

    /**
     * @return table name
     */
    @Nonnull
    abstract public String sourceTableName();
}
