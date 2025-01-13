package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.statement.templated.TemplateArgumentMapping;
import io.github.sinri.keel.mysql.statement.templated.TemplatedModifyStatement;
import io.github.sinri.keel.mysql.statement.templated.TemplatedReadStatement;
import io.github.sinri.keel.mysql.statement.templated.TemplatedStatement;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.0.9
 */
public interface AnyStatement {


    /**
     * @since 3.0.9
     */
    static AbstractStatement raw(@Nonnull String sql) {
        return new AbstractStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * @since 3.2.21 return AbstractReadStatement
     */
    static AbstractReadStatement select(@Nonnull Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement;
    }

    /**
     * @since 3.2.21 return AbstractReadStatement
     */
    static AbstractReadStatement union(@Nonnull Handler<UnionStatement> unionStatementHandler) {
        UnionStatement unionStatement = new UnionStatement();
        unionStatementHandler.handle(unionStatement);
        return unionStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static AbstractModifyStatement update(@Nonnull Handler<UpdateStatement> updateStatementHandler) {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatementHandler.handle(updateStatement);
        return updateStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static AbstractModifyStatement delete(@Nonnull Handler<DeleteStatement> deleteStatementHandler) {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatementHandler.handle(deleteStatement);
        return deleteStatement;
    }

    /**
     * @since 3.2.21 return AbstractWriteIntoStatement
     */
    static AbstractWriteIntoStatement insert(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.INSERT);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static AbstractModifyStatement replace(@Nonnull Handler<WriteIntoStatement> statementHandler) {
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
     * @since 3.2.21 return AbstractReadStatement
     */
    static AbstractReadStatement templatedRead(@Nonnull String path, @Nonnull Handler<TemplateArgumentMapping> templatedReadStatementHandler) {
        TemplatedReadStatement readStatement = TemplatedStatement.loadTemplateToRead(path);
        TemplateArgumentMapping arguments = readStatement.getArguments();
        templatedReadStatementHandler.handle(arguments);
        return readStatement;
    }

    /**
     * @since 3.2.21 return AbstractModifyStatement
     */
    static AbstractModifyStatement templatedModify(@Nonnull String path, @Nonnull Handler<TemplateArgumentMapping> templatedModifyStatementHandler) {
        TemplatedModifyStatement templatedModifyStatement = TemplatedStatement.loadTemplateToModify(path);
        TemplateArgumentMapping arguments = templatedModifyStatement.getArguments();
        templatedModifyStatementHandler.handle(arguments);
        return templatedModifyStatement;
    }

    /**
     * @return The SQL Generated
     */
    String toString();

    Future<ResultMatrix> execute(@Nonnull SqlConnection sqlConnection);

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<ResultMatrix> execute(@Nonnull NamedMySQLConnection namedSqlConnection) {
        return execute(namedSqlConnection.getSqlConnection());
    }
}
