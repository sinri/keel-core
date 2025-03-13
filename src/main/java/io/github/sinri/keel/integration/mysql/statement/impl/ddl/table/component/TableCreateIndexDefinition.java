package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 4.0.4
 */
public abstract class TableCreateIndexDefinition extends TableCreateDefinition {
    /**
     * Each as {@code key_part: {col_name [(length)] | (expr)} [ASC | DESC] }
     */
    private final List<String> keyParts = new ArrayList<>();

    private @Nullable String indexType = null;
    private @Nullable String indexOption = null;

    public TableCreateIndexDefinition addKeyPart(String keyPart) {
        this.keyParts.add(keyPart);
        return this;
    }

    protected List<String> getKeyParts() {
        return keyParts;
    }

    protected String getKeyPartsExpression() {
        String s = Keel.stringHelper().joinStringArray(
                keyParts.stream().map(x -> "`" + x + "`").collect(Collectors.toList()),
                ", "
        );
        return "(" + s + ")";
    }

    @Nullable
    protected String getIndexType() {
        return indexType;
    }

    public TableCreateIndexDefinition setIndexType(@Nullable String indexType) {
        this.indexType = indexType;
        return this;
    }

    @Nullable
    protected String getIndexOption() {
        return indexOption;
    }

    public TableCreateIndexDefinition setIndexOption(@Nullable String indexOption) {
        this.indexOption = indexOption;
        return this;
    }
}
