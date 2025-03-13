package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * {@code ADD {INDEX | KEY} [index_name] [index_type] (key_part,...) [index_option] ...}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToAddKey extends TableAlterOption {
    private @Nullable String indexName = null;
    private @Nullable String indexType = null;
    private final List<String> keyParts = new ArrayList<>();
    private @Nullable String indexOption = null;

    public TableAlterOptionToAddKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    public TableAlterOptionToAddKey setIndexType(@Nullable String indexType) {
        this.indexType = indexType;
        return this;
    }

    public TableAlterOptionToAddKey setIndexOption(@Nullable String indexOption) {
        this.indexOption = indexOption;
        return this;
    }

    public TableAlterOptionToAddKey addKeyPart(@Nonnull String keyPart) {
        this.keyParts.add(keyPart);
        return this;
    }

    @Override
    public String toString() {
        List<String> list = keyParts.stream().map(x -> "`" + x + "`").collect(Collectors.toList());
        return "ADD INDEX "
                + (indexName != null ? ("`" + indexName + "`") : "") + " "
                + (indexType != null ? (indexType) : "") + " "
                + "("
                + Keel.stringHelper().joinStringArray(list, ", ")
                + ") "
                + (indexOption != null ? (indexOption) : "");
    }
}
