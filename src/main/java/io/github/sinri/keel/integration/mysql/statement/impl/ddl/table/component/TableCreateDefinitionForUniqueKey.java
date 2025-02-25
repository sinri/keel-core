package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

import javax.annotation.Nullable;

/**
 * @since 4.0.4
 */
public final class TableCreateDefinitionForUniqueKey extends TableCreateIndexDefinition {
    private @Nullable String constraintSymbol;
    private @Nullable String indexName = null;

    public TableCreateDefinitionForUniqueKey setConstraintSymbol(@Nullable String constraintSymbol) {
        this.constraintSymbol = constraintSymbol;
        return this;
    }

    public TableCreateDefinitionForUniqueKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        String indexType = getIndexType();
        String indexOption = getIndexOption();
        return (constraintSymbol != null ? ("CONSTRAINT " + constraintSymbol + " UNIQUE KEY") : "") + " "
                + (indexName != null ? indexName : "") + " "
                + (indexType != null ? indexType : "") + " "
                + getKeyPartsExpression() + " "
                + (indexOption != null ? indexOption : "") + " ";

    }
}
