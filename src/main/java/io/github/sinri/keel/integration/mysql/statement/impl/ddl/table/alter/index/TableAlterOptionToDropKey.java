package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;

import javax.annotation.Nonnull;

/**
 * {@code DROP {INDEX | KEY} index_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToDropKey extends TableAlterOption {
    private @Nonnull String indexName = "";

    public TableAlterOptionToDropKey setIndexName(@Nonnull String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        return "DROP KEY `" + indexName + "`";
    }
}
