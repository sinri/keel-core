package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.integration.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLException;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Pair data source to a named mysql connection.
 * <p>
 * As of 3.0.18, Finished Technical Preview.
 *
 * @param <C> the type of connection
 * @since 3.0.11
 */
public final class NamedMySQLDataSource<C extends NamedMySQLConnection> {

    private final Pool pool;
    private final KeelMySQLConfiguration configuration;

    /**
     * Hold the count of the connections initialized to pool.
     *
     * @since 4.1.5
     */
    private final AtomicInteger initializedConnectionCounter = new AtomicInteger(0);
    /**
     * Hold the count of the connections currently used.
     *
     * @since 4.1.5
     */
    private final AtomicInteger borrowedConnectionCounter = new AtomicInteger(0);

    private final Function<SqlConnection, C> sqlConnectionWrapper;

    private final AtomicReference<String> fullVersionRef = new AtomicReference<>(null);

    public NamedMySQLDataSource(
            @Nonnull KeelMySQLConfiguration configuration,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper) {
        this(configuration, sqlConnection -> Future.succeededFuture(), sqlConnectionWrapper);
    }

    /**
     * @since 3.0.2
     */
    public NamedMySQLDataSource(
            @Nonnull KeelMySQLConfiguration configuration,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper) {
        this.configuration = configuration;
        this.sqlConnectionWrapper = sqlConnectionWrapper;
        this.pool = MySQLBuilder.pool()
                                .with(configuration.getPoolOptions())
                                .connectingTo(configuration.getConnectOptions())
                                .using(Keel.getVertx())
                                .withConnectHandler(sqlConnection -> initializeConnection(sqlConnection, connectionSetUpFunction))
                                .build();
    }

    /**
     * @since 3.1.0
     */
    private static Future<String> checkMySQLVersion(@Nonnull SqlConnection sqlConnection) {
        return sqlConnection.preparedQuery("SELECT VERSION() as v; ")
                            .execute()
                            .compose(rows -> Future.succeededFuture(ResultMatrix.create(rows)))
                            .compose(resultMatrix -> {
                                try {
                                    JsonObject firstRow = resultMatrix.getFirstRow();
                                    String versionExp = firstRow.getString("v");
                                    return Future.succeededFuture(versionExp);
                                } catch (Throwable e) {
                                    Keel.getLogger().exception(e);
                                    return Future.succeededFuture(null);
                                }
                            });
    }

    /**
     * Do initialization for a new established connection before releasing it into
     * the pool with a
     * {@link SqlClient#close()} call.
     *
     * @see ClientBuilder#withConnectHandler(Handler)
     * @since 4.1.5
     */
    private void initializeConnection(
            @Nonnull SqlConnection sqlConnection,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction
    ) {
        Future.succeededFuture()
              .compose(v -> {
                  if (connectionSetUpFunction != null) {
                      return connectionSetUpFunction.apply(sqlConnection);
                  } else {
                      return Future.succeededFuture();
                  }
              })
              .compose(v -> {
                  if (fullVersionRef.get() == null) {
                      return checkMySQLVersion(sqlConnection)
                              .compose(ver -> {
                                  if (ver != null) {
                                      fullVersionRef.set(ver);
                                  }
                                  return Future.succeededFuture();
                              });
                  } else {
                      return Future.succeededFuture();
                  }
              })
              .onComplete(ar -> {
                  sqlConnection.close()
                               .onSuccess(releasedConnectionToPool -> {
                                   initializedConnectionCounter.incrementAndGet();
                               });
              });
    }

    public KeelMySQLConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * To get the connections initialized in the pool and not in use right now.
     *
     * @return the number of connections initialized in the pool and not in use
     *         right now
     * @since 3.0.2
     * @deprecated As of 4.1.5, changed the implementation, use
     *         {@link NamedMySQLDataSource#getCurrentIdleConnectionCount()}
     *         instead.
     */
    @Deprecated(since = "4.1.5")
    public int getAvailableConnectionCount() {
        return getCurrentIdleConnectionCount();
    }

    /**
     * To get the count of the connections initialized in the pool and not in use
     * right now.
     *
     * @return the number of connections initialized in the pool and not in use
     *         right now
     * @since 4.1.5
     */
    public int getCurrentIdleConnectionCount() {
        return getCurrentInitializedConnectionCount() - getCurrentActiveConnectionCount();
    }

    /**
     * To get the count of the connections initialized in the pool.
     *
     * @return the number of connections initialized in the pool
     * @since 4.1.5
     */
    public int getCurrentInitializedConnectionCount() {
        return initializedConnectionCounter.get();
    }

    /**
     * To get the count of the connections currently used, i.e. borrowed from the
     * pool.
     *
     * @return the number of connections currently used
     * @since 4.1.5
     */
    public int getCurrentActiveConnectionCount() {
        return borrowedConnectionCounter.get();
    }

    /**
     * @since 3.1.0
     */
    public @Nullable String getFullVersionRef() {
        return fullVersionRef.get();
    }

    public <T> Future<T> withConnection(@Nonnull Function<C, Future<T>> function) {
        return Future.succeededFuture()
                     .compose(v -> fetchMySQLConnection()
                             .compose(sqlConnectionWrapper -> {
                                 borrowedConnectionCounter.incrementAndGet();
                                 return Future.succeededFuture()
                                              .compose(vv -> function.apply(sqlConnectionWrapper))
                                              .andThen(tAsyncResult -> Future.succeededFuture()
                                                                             .compose(vv -> sqlConnectionWrapper.getSqlConnection()
                                                                                                                .close())
                                                                             .andThen(ar -> {
                                                                                 borrowedConnectionCounter.decrementAndGet();
                                                                             }))
                                              .recover(throwable -> Future.failedFuture(new KeelMySQLException(
                                                      "MySQLDataSource Failed Within SqlConnection: " + throwable,
                                                      throwable)));
                             }));
    }

    public <T> Future<T> withTransaction(@Nonnull Function<C, Future<T>> function) {
        return withConnection(c -> {
            return Future.succeededFuture()
                         .compose(v -> {
                             return c.getSqlConnection().begin();
                         })
                         .compose(
                                 transaction -> Future.succeededFuture()
                                                      .compose(v -> {
                                                          // execute and commit
                                                          return function.apply(c)
                                                                         .compose(t -> transaction.commit()
                                                                                                  .compose(committed -> Future.succeededFuture(t)));
                                                      })
                                                      .compose(Future::succeededFuture, err -> {
                                                          if (err instanceof TransactionRollbackException) {
                                                              // already rollback
                                                              String error = "MySQLDataSource ROLLBACK Done Manually.";
                                                              return Future.failedFuture(new KeelMySQLException(error, err));
                                                          } else {
                                                              String error = "MySQLDataSource ROLLBACK Finished. Core Reason: "
                                                                      + err.getMessage();
                                                              // since 3.0.3 rollback failure would be thrown directly to downstream.
                                                              return transaction.rollback()
                                                                                .compose(rollbackDone -> Future
                                                                                        .failedFuture(new KeelMySQLException(error, err)));
                                                          }
                                                      }),
                                 beginFailure -> Future.failedFuture(new KeelMySQLConnectionException(
                                         "MySQLDataSource Failed to get SqlConnection for transaction From Pool: "
                                                 + beginFailure,
                                         beginFailure)));
        });
    }

    /**
     * @since 3.0.5
     */
    public Future<Void> close() {
        return this.pool.close();
    }

    /**
     * @since 3.0.5
     */
    public void close(@Nonnull Handler<AsyncResult<Void>> ar) {
        this.pool.close().onComplete(ar);
    }

    private Future<C> fetchMySQLConnection() {
        return Future.succeededFuture()
                     .compose(v -> pool.getConnection())
                     .compose(
                             sqlConnection -> {
                                 C c = this.sqlConnectionWrapper.apply(sqlConnection);

                                 // since 3.1.0: add mysql version to c;
                                 c.setMysqlVersion(this.fullVersionRef.get());

                                 return Future.succeededFuture(c);
                             },
                             throwable -> Future.failedFuture(
                                     new KeelMySQLConnectionException(
                                             "MySQLDataSource Failed to get SqlConnection From Pool " +
                                                     "`" + this.getConfiguration().getDataSourceName() + "` " +
                                                     "(usage: " + borrowedConnectionCounter.get() + " of "
                                                     + initializedConnectionCounter.get() + "): " +
                                                     throwable,
                                             throwable)));
    }
}
