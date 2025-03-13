package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component.*;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Follow Pattern:
 * <p>
 * CREATE TABLE:
 * {@code CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name (create_definition,...) [table_options] [partition_options]
 * }
 * </p>
 * <p>
 * CRETE TABLE AS QUERY:
 * {@code CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name [(create_definition,...)] [table_options]
 * [partition_options] [IGNORE | REPLACE] [AS] query_expression }.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/create-table.html">CREATE TABLE Statement</a> MySQL 8.0
 *         Docs
 * @since 4.0.4
 */
public class CreateTableStatement extends CreateTableStatementBase<CreateTableStatement> {
    private final List<TableCreateDefinition> definitions = new ArrayList<>();
    private final CreateTableOptions tableOptions = new CreateTableOptions();
    private @Nullable CreateTablePartitionOptions partitionOptions;
    /**
     * IGNORE or REPLACE
     */
    private @Nullable String asSourceType = null;
    private ReadStatementMixin readStatement = null;

    public CreateTableStatement asReadStatement(@Nonnull ReadStatementMixin readStatement) {
        this.readStatement = readStatement;
        return getImplementation();
    }

    public CreateTableStatement asReadStatementWithIgnore(@Nonnull ReadStatementMixin readStatement) {
        this.asSourceType = "IGNORE";
        this.readStatement = readStatement;
        return getImplementation();
    }

    public CreateTableStatement asReadStatementWithReplace(@Nonnull ReadStatementMixin readStatement) {
        this.asSourceType = "REPLACE";
        this.readStatement = readStatement;
        return getImplementation();
    }

    @Override
    public String toString() {
        var ds = definitions.stream().map(Object::toString).collect(Collectors.toList());
        var sql = "CREATE " + (useTemporary() ? "TEMPORARY " : " ") + "TABLE "
                + (useIfNotExists() ? "IF NOT EXISTS " : " ")
                + getTableExpression() + " " + SQL_COMPONENT_SEPARATOR
                + "(" + SQL_COMPONENT_SEPARATOR
                + Keel.stringHelper().joinStringArray(ds, ", " + SQL_COMPONENT_SEPARATOR)
                + SQL_COMPONENT_SEPARATOR
                + ") " + SQL_COMPONENT_SEPARATOR
                + tableOptions + " " + SQL_COMPONENT_SEPARATOR
                + (partitionOptions == null ? "" : partitionOptions) + " " + SQL_COMPONENT_SEPARATOR;
        if (asSourceType != null) {
            sql += " " + asSourceType + SQL_COMPONENT_SEPARATOR
                    + " " + Objects.requireNonNull(readStatement);
        }
        return sql;
    }

    public CreateTableStatement setPartitionOptions(@Nullable CreateTablePartitionOptions partitionOptions) {
        this.partitionOptions = partitionOptions;
        return getImplementation();
    }

    public CreateTableStatement handleTableOptions(@Nonnull Handler<CreateTableOptions> tableOptionHandler) {
        tableOptionHandler.handle(tableOptions);
        return getImplementation();
    }

    public CreateTableStatement addDefinition(TableCreateDefinition tableCreateDefinition) {
        this.definitions.add(tableCreateDefinition);
        return getImplementation();
    }

    public CreateTableStatement addColumnDefinition(@Nonnull Handler<TableCreateDefinitionForColumn> columnDefinitionHandler) {
        TableCreateDefinitionForColumn tableCreateDefinitionForColumn = new TableCreateDefinitionForColumn();
        columnDefinitionHandler.handle(tableCreateDefinitionForColumn);
        return this.addDefinition(tableCreateDefinitionForColumn);
    }

    public CreateTableStatement addPrimaryKeyDefinition(@Nonnull Handler<TableCreateDefinitionForPrimaryKey> primaryKeyDefinitionHandler) {
        TableCreateDefinitionForPrimaryKey tableCreateDefinitionForPrimaryKey = new TableCreateDefinitionForPrimaryKey();
        primaryKeyDefinitionHandler.handle(tableCreateDefinitionForPrimaryKey);
        return this.addDefinition(tableCreateDefinitionForPrimaryKey);
    }

    public CreateTableStatement addUniqueKeyDefinition(@Nonnull Handler<TableCreateDefinitionForUniqueKey> uniqueKeyDefinitionHandler) {
        TableCreateDefinitionForUniqueKey tableCreateDefinitionForUniqueKey = new TableCreateDefinitionForUniqueKey();
        uniqueKeyDefinitionHandler.handle(tableCreateDefinitionForUniqueKey);
        return this.addDefinition(tableCreateDefinitionForUniqueKey);
    }

    public CreateTableStatement addKeyDefinition(@Nonnull Handler<TableCreateDefinitionForKey> keyDefinitionHandler) {
        TableCreateDefinitionForKey tableCreateDefinitionForKey = new TableCreateDefinitionForKey();
        keyDefinitionHandler.handle(tableCreateDefinitionForKey);
        return this.addDefinition(tableCreateDefinitionForKey);
    }

    @Nonnull
    @Override
    public CreateTableStatement getImplementation() {
        return this;
    }
}
