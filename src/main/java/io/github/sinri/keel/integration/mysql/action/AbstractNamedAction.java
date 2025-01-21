package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;

import javax.annotation.Nonnull;

/**
 * Abstract class that implements the NamedActionInterface for handling named MySQL connections.
 * This class provides a constructor to initialize the named MySQL connection and implements the getNamedSqlConnection method to return the connection.
 *
 * @param <C> a specific connection class that extends NamedMySQLConnection
 * @since 3.2.11 Moved from `io.github.sinri.keel.mysql.AbstractNamedAction` and refined.
 */
public abstract class AbstractNamedAction<C extends NamedMySQLConnection> implements NamedActionInterface<C> {
    private final @Nonnull C namedSqlConnection;

    public AbstractNamedAction(@Nonnull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    @Nonnull
    @Override
    public C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
