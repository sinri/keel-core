package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.impl.*;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.TruncateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.AlterTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableLikeTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.AlterViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.CreateViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.DropViewStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.SelectStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.WriteIntoStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplateArgumentMapping;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedModifyStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedReadStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;

/**
 * @since 3.0.9
 */
public interface AnyStatement {

    /**
     * @since 3.0.9
     */
    static AbstractStatement raw(@Nonnull String sql) {
        return raw(sql, false);
    }


    /**
     * Creates an AbstractStatement with the provided SQL and a flag indicating whether to use prepared statements.
     *
     * @param sql            the raw SQL statement
     * @param withoutPrepare a boolean value indicating whether to execute the statement without preparing it
     * @return an instance of AbstractStatement
     * @since 4.0.7
     */
    static AbstractStatement raw(@Nonnull String sql, boolean withoutPrepare) {
        return new AbstractStatement() {
            @Override
            public String toString() {
                return sql;
            }

            @Override
            public boolean isWithoutPrepare() {
                return withoutPrepare;
            }
        };
    }

    /**
     * @since 3.2.21 return AbstractReadStatement
     */
    static SelectStatementMixin select(@Nonnull Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement;
    }

    /**
     * @since 3.2.21 return AbstractReadStatement
     */
    static ReadStatementMixin union(@Nonnull Handler<UnionStatement> unionStatementHandler) {
        UnionStatement unionStatement = new UnionStatement();
        unionStatementHandler.handle(unionStatement);
        return unionStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static ModifyStatementMixin update(@Nonnull Handler<UpdateStatement> updateStatementHandler) {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatementHandler.handle(updateStatement);
        return updateStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static ModifyStatementMixin delete(@Nonnull Handler<DeleteStatement> deleteStatementHandler) {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatementHandler.handle(deleteStatement);
        return deleteStatement;
    }

    /**
     * @since 3.2.21 return AbstractWriteIntoStatement
     */
    static WriteIntoStatementMixin insert(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.INSERT);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static WriteIntoStatementMixin replace(@Nonnull Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.REPLACE);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    /**
     * @since 3.2.19
     * @since 3.2.21 return AbstractStatement
     */
    static AbstractStatement call(@Nonnull Handler<CallStatement> statementHandler) {
        CallStatement callStatement = new CallStatement();
        statementHandler.handle(callStatement);
        return callStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement truncateTable(@Nonnull Handler<TruncateTableStatement> statementHandler) {
        TruncateTableStatement truncateTableStatement = new TruncateTableStatement();
        statementHandler.handle(truncateTableStatement);
        return truncateTableStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement createTable(@Nonnull Handler<CreateTableStatement> statementHandler) {
        CreateTableStatement createTableStatement = new CreateTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement createTableLikeTable(@Nonnull Handler<CreateTableLikeTableStatement> statementHandler) {
        CreateTableLikeTableStatement createTableStatement = new CreateTableLikeTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement alterTable(@Nonnull Handler<AlterTableStatement> statementHandler) {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        statementHandler.handle(alterTableStatement);
        return alterTableStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement createView(@Nonnull Handler<CreateViewStatement> statementHandler) {
        CreateViewStatement createViewStatement = new CreateViewStatement();
        statementHandler.handle(createViewStatement);
        return createViewStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement alterView(@Nonnull Handler<AlterViewStatement> statementHandler) {
        AlterViewStatement alterViewStatement = new AlterViewStatement();
        statementHandler.handle(alterViewStatement);
        return alterViewStatement;
    }

    /**
     * @since 4.0.4
     */
    static AbstractStatement dropView(@Nonnull Handler<DropViewStatement> statementHandler) {
        DropViewStatement dropViewStatement = new DropViewStatement();
        statementHandler.handle(dropViewStatement);
        return dropViewStatement;
    }

    /**
     * @since 3.2.21 return AbstractReadStatement
     */
    static ReadStatementMixin templatedRead(@Nonnull String path, @Nonnull Handler<TemplateArgumentMapping> templatedReadStatementHandler) {
        TemplatedReadStatement readStatement = TemplatedStatement.loadTemplateToRead(path);
        TemplateArgumentMapping arguments = readStatement.getArguments();
        templatedReadStatementHandler.handle(arguments);
        return readStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static ModifyStatementMixin templatedModify(@Nonnull String path, @Nonnull Handler<TemplateArgumentMapping> templatedModifyStatementHandler) {
        TemplatedModifyStatement templatedModifyStatement = TemplatedStatement.loadTemplateToModify(path);
        TemplateArgumentMapping arguments = templatedModifyStatement.getArguments();
        templatedModifyStatementHandler.handle(arguments);
        return templatedModifyStatement;
    }

    /**
     * @return The SQL Generated
     */
    String toString();

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    Future<ResultMatrix> execute(@Nonnull NamedMySQLConnection namedSqlConnection);

    boolean isWithoutPrepare();
}
