package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;

import javax.annotation.Nonnull;

/**
 * Class that defines the interface for named MySQL actions in a mixin style.
 * <p>
 * This interface extends {@link SelfInterface} to provide a method to retrieve
 * the named MySQL connection associated with the action,
 * with this connection, you can execute SQL statements.
 * All your actions on MySQL connection should be wrapped in a transaction
 * managed by higher level;
 * i.e. do not manage transactions within any action.
 *
 * @param <C> a specific connection class that extends NamedMySQLConnection
 * @param <W> a generic type representing the mixin or additional context
 * @since 3.2.11
 */
public interface NamedActionMixinInterface<C extends NamedMySQLConnection, W>
        extends SelfInterface<W> {
    /**
     * Retrieves the associated named MySQL connection.
     *
     * @return the named MySQL connection instance associated with this action; never null
     */
    @Nonnull
    C getNamedSqlConnection();
}
