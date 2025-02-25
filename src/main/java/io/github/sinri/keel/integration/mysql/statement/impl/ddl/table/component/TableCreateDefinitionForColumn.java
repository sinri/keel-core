package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

import io.github.sinri.keel.integration.mysql.Quoter;

import javax.annotation.Nullable;

/**
 * @since 4.0.4
 */
public final class TableCreateDefinitionForColumn extends TableCreateDefinition {
    private String columnName;
    //private ColumnDefinition columnDefinition;
    private String dataType;
    private boolean nullable;
    private @Nullable String defaultExpression;
    private boolean autoIncrement = false;
    private @Nullable String comment;
    private @Nullable String collationName;

    private @Nullable String others;

    public TableCreateDefinitionForColumn setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    @Override
    public String toString() {
        return "`" + columnName + "` "
                + dataType + " " + (nullable ? "NULL" : "NOT NULL") + " "
                + (defaultExpression != null ? ("DEFAULT " + new Quoter(defaultExpression)) : "") + " "
                + (autoIncrement ? "AUTO_INCREMENT" : "") + " "
                + (comment != null ? ("COMMENT " + new Quoter(comment)) : "") + " "
                + (collationName != null ? ("COLLATE " + collationName) : "") + " "
                + (others != null ? (others) : "") + " ";
    }


    public TableCreateDefinitionForColumn setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public TableCreateDefinitionForColumn setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public TableCreateDefinitionForColumn setDefaultExpression(@Nullable String defaultExpression) {
        this.defaultExpression = defaultExpression;
        return this;
    }

    public TableCreateDefinitionForColumn setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public TableCreateDefinitionForColumn setCollationName(@Nullable String collationName) {
        this.collationName = collationName;
        return this;
    }

    public TableCreateDefinitionForColumn setComment(@Nullable String comment) {
        this.comment = comment;
        return this;
    }

    public TableCreateDefinitionForColumn setOthers(@Nullable String others) {
        this.others = others;
        return this;
    }

}
