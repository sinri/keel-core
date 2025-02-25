package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 4.0.4
 */
public abstract class CreateTableStatementBase<T> extends AbstractStatement implements SelfInterface<T> {
    private boolean temporary = false;
    private boolean ifNotExists = false;
    private @Nullable String schemaName = null;
    private @Nonnull String tableName = "";

    public T setTemporary(boolean temporary) {
        this.temporary = temporary;
        return getImplementation();
    }

    public T setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
        return getImplementation();
    }

    protected boolean useTemporary() {
        return temporary;
    }

    protected boolean useIfNotExists() {
        return ifNotExists;
    }

    @Nullable
    protected String getSchemaName() {
        return schemaName;
    }

    public T setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return getImplementation();
    }

    @Nonnull
    protected String getTableName() {
        return tableName;
    }

    public T setTableName(@Nonnull String tableName) {
        this.tableName = tableName;
        return getImplementation();
    }

    protected final String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`.")) + ("`" + tableName + "`");
    }

    /**
     * @return the generated DDL SQL.
     */
    @Override
    abstract public String toString();
}
