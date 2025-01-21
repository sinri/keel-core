package io.github.sinri.keel.mysql.statement.mixin;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

/**
 * @since 3.2.21
 */
public interface SelectStatementMixin extends ReadStatementMixin {
    /**
     * Call from this instance, as the original query as Select Statement for all rows in certain order.
     *
     * @param pageNo   since 1.
     * @param pageSize a number
     * @since 3.2.3
     * @since 3.2.20 Public
     */
    Future<PaginationResult> queryForPagination(
            SqlConnection sqlConnection,
            long pageNo,
            long pageSize
    );

    /**
     * @since 3.2.3
     * @since 3.2.20 Public
     */
    default Future<PaginationResult> queryForPagination(
            NamedMySQLConnection sqlConnection,
            long pageNo,
            long pageSize
    ) {
        return this.queryForPagination(sqlConnection.getSqlConnection(), pageNo, pageSize);
    }

    class PaginationResult {
        private final long total;
        private final ResultMatrix resultMatrix;

        public PaginationResult(long total, ResultMatrix resultMatrix) {
            this.total = total;
            this.resultMatrix = resultMatrix;
        }

        public long getTotal() {
            return total;
        }

        public ResultMatrix getResultMatrix() {
            return resultMatrix;
        }
    }
}
