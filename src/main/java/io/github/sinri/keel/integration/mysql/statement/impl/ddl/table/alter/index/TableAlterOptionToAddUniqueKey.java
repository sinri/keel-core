package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * {@code ADD [CONSTRAINT [symbol]] UNIQUE [INDEX | KEY] [index_name] [index_type] (key_part,...) [index_option] ...}
 *
 * @since 4.0.4
 */
public class TableAlterOptionToAddUniqueKey extends TableAlterOption {
    private final List<String> keyParts = new ArrayList<>();
    private @Nullable String constraintSymbol = null;
    private @Nullable String indexName = null;
    private @Nullable String indexType = null;
    private @Nullable String indexOption = null;

    public TableAlterOptionToAddUniqueKey setConstraintSymbol(@Nullable String constraintSymbol) {
        this.constraintSymbol = constraintSymbol;
        return this;
    }

    public TableAlterOptionToAddUniqueKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    public TableAlterOptionToAddUniqueKey setIndexType(@Nullable String indexType) {
        this.indexType = indexType;
        return this;
    }

    public TableAlterOptionToAddUniqueKey setIndexOption(@Nullable String indexOption) {
        this.indexOption = indexOption;
        return this;
    }

    public TableAlterOptionToAddUniqueKey addPartKey(@Nonnull String keyPart) {
        this.keyParts.add(keyPart);
        return this;
    }

    @Override
    public String toString() {
        List<String> list = keyParts.stream().map(x -> "`" + x + "`").collect(Collectors.toList());

        return "ADD "
                + (constraintSymbol == null ? "" : ("CONSTRAINT `" + constraintSymbol + "`")) + " "
                + "UNIQUE KEY "
                + (indexName == null ? "" : ("`" + indexName + "`")) + " "
                + (indexType == null ? "" : (indexType)) + " "
                + "(" + Keel.stringHelper().joinStringArray(list, ", ") + ") "
                + (indexOption == null ? "" : (indexOption)) + " ";
    }
}
