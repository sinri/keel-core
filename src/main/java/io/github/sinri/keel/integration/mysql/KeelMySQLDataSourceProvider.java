package io.github.sinri.keel.integration.mysql;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelMySQLDataSourceProvider {

    @Nonnull
    public static String defaultMySQLDataSourceName() {
        return Objects.requireNonNull(Keel.getConfiguration()
                                          .readString(List.of("mysql", "default_data_source_name"), "default"));
    }

    /**
     * As of 3.0.18 Finished Technical Preview.
     *
     * @since 3.0.11 Technical Preview.
     *
     */
    public static <C extends NamedMySQLConnection> NamedMySQLDataSource<C> initializeNamedMySQLDataSource(
            @Nonnull String dataSourceName,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        return initializeNamedMySQLDataSource(dataSourceName, sqlConnectionWrapper, null, Promise.promise());
    }

    /**
     * Initialize a named MySQL Data Source and return the future after actual availability confirmed.
     *
     * @since 4.1.5
     */
    public static <C extends NamedMySQLConnection> Future<NamedMySQLDataSource<C>> loadNamedMySQLDataSource(
            @Nonnull String dataSourceName,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        Promise<Void> initializedPromise = Promise.promise();
        var dataSource = initializeNamedMySQLDataSource(dataSourceName, sqlConnectionWrapper, null, initializedPromise);
        return initializedPromise.future().map(v -> dataSource);
    }

    /**
     * @since 4.1.5
     */
    public static <C extends NamedMySQLConnection> NamedMySQLDataSource<C> initializeNamedMySQLDataSource(
            @Nonnull String dataSourceName,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            Promise<Void> initializedPromise
    ) {
        var configuration = Keel.getConfiguration().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        KeelMySQLConfiguration mySQLConfigure = new KeelMySQLConfiguration(configuration);
        if (connectionSetUpFunction == null) {
            connectionSetUpFunction = sqlConnection -> Future.succeededFuture();
        }
        var dataSource = new NamedMySQLDataSource<>(mySQLConfigure, connectionSetUpFunction, sqlConnectionWrapper);

        Keel.getVertx().setTimer(
                mySQLConfigure.getPoolOptions().getConnectionTimeout() * 1000L,
                x -> {
                    initializedPromise.tryFail("MySQL Pool Connection Timeout on testing, the configuration might need adjusting.");
                });
        dataSource.withConnection(c -> {
            initializedPromise.tryComplete();
            return Future.succeededFuture();
        });

        return dataSource;
    }

    /**
     * @since 3.0.11 Technical Preview.
     * @since 3.0.18 Finished Technical Preview.
     */
    public static NamedMySQLDataSource<DynamicNamedMySQLConnection> initializeDynamicNamedMySQLDataSource(@Nonnull String dataSourceName) {
        return initializeNamedMySQLDataSource(dataSourceName, sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, dataSourceName));
    }
}
