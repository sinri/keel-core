package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;

import javax.annotation.Nonnull;


/**
 * Abstract class that implements the NamedActionMixinInterface for handling named MySQL connections in a mixin style.
 * This class provides a constructor to initialize the named MySQL connection and implements the getNamedSqlConnection method to return the connection.
 *
 * @param <C> a specific connection class that extends NamedMySQLConnection
 * @param <W> a generic type representing the mixin or additional context
 * @since 3.2.11 Refined for Mixin Style, extracted NamedActionInterface.
 */
public abstract class AbstractNamedMixinAction<C extends NamedMySQLConnection, W> implements NamedActionMixinInterface<C, W> {
    private final @Nonnull C namedSqlConnection;

    /**
     * Constructs an AbstractNamedMixinAction with the specified named MySQL connection.
     *
     * @param namedSqlConnection the named MySQL connection instance to be associated with this action; must not be null
     */
    public AbstractNamedMixinAction(@Nonnull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    /**
     * Retrieves the named MySQL connection associated with this action.
     *
     * @return the named MySQL connection instance tied to this action; never null
     */
    @Nonnull
    @Override
    public final C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
