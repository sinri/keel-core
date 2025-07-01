package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.7
 */
abstract public class AbstractStatement implements AnyStatement {
    protected static @Nonnull String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    /**
     * @since 3.2.0 replace original SQL Audit Logger
     */
    protected static @Nonnull KeelIssueRecorder<MySQLAuditIssueRecord> sqlAuditIssueRecorder;

    static {
        sqlAuditIssueRecorder = buildSqlAuditIssueRecorder(KeelIssueRecordCenter.silentCenter());
    }

    protected final @Nonnull String statement_uuid;
    private @Nonnull String remarkAsComment = "";
    /**
     * @since 4.0.7
     */
    private boolean withoutPrepare = false;

    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    @Nonnull
    public static KeelIssueRecorder<MySQLAuditIssueRecord> getSqlAuditIssueRecorder() {
        return sqlAuditIssueRecorder;
    }

    /**
     * @since 4.0.0
     */
    private static KeelIssueRecorder<MySQLAuditIssueRecord> buildSqlAuditIssueRecorder(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        return issueRecordCenter.generateIssueRecorder(MySQLAuditIssueRecord.AttributeMysqlAudit, MySQLAuditIssueRecord::new);
    }

    /**
     * Change the SQL Audit Issue Recorder.
     *
     * @param issueRecordCenter with which issue record center the sql audit sent to.
     * @since 4.0.0
     */
    public static synchronized void reloadSqlAuditIssueRecording(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        sqlAuditIssueRecorder = buildSqlAuditIssueRecorder(issueRecordCenter);
    }

    public static void setSqlComponentSeparator(@Nonnull String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    @Nonnull
    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    public AbstractStatement setRemarkAsComment(@Nonnull String remarkAsComment) {
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return this;
    }

    /**
     * 在给定的SqlConnection上执行SQL，异步返回ResultMatrix，或异步报错。
     * （如果SQL审计日志记录器可用）将为审计记录执行的SQL和执行结果，以及任何异常。
     *
     * @param namedSqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     * @since 2.8 将整个运作体加入了try-catch，统一加入审计日志，出现异常时一律异步报错。
     * @since 3.0.0 removed try-catch
     */
    @Override
    public Future<ResultMatrix> execute(@Nonnull NamedMySQLConnection namedSqlConnection) {
        AtomicReference<String> theSql = new AtomicReference<>();
        return Future.succeededFuture(this.toString())
                     .compose(sql -> {
                         theSql.set(sql);

                         if (isWithoutPrepare()) {
                             getSqlAuditIssueRecorder().info(r -> r.setQuery(statement_uuid, sql));
                             return namedSqlConnection.getSqlConnection().query(sql).execute();
                         } else {
                             getSqlAuditIssueRecorder().info(r -> r.setPreparation(statement_uuid, sql));
                             return namedSqlConnection.getSqlConnection().preparedQuery(sql).execute();
                         }
                     })
                     .compose(rows -> {
                         ResultMatrix resultMatrix = ResultMatrix.create(rows);
                         return Future.succeededFuture(resultMatrix);
                     })
                     .compose(resultMatrix -> {
                         getSqlAuditIssueRecorder().info(r -> r.setForDone(statement_uuid, theSql.get(), resultMatrix.getTotalAffectedRows(), resultMatrix.getTotalFetchedRows()));
                         return Future.succeededFuture(resultMatrix);
                     }, throwable -> {
                         getSqlAuditIssueRecorder().exception(throwable, r -> r.setForFailed(statement_uuid, theSql.get()));
                         return Future.failedFuture(throwable);
                     });
    }

    /**
     * @since 4.0.7
     */
    @Override
    public boolean isWithoutPrepare() {
        return withoutPrepare;
    }

    /**
     * @since 4.0.7
     */
    public AbstractStatement setWithoutPrepare(boolean withoutPrepare) {
        this.withoutPrepare = withoutPrepare;
        return this;
    }
}
