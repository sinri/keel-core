package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;

import javax.annotation.Nonnull;

/**
 * Class that defines the interface for named MySQL actions.
 * <p>
 * This interface provides a method to retrieve the named MySQL connection
 * associated with the action,
 * with this connection, you can execute SQL statements.
 * All your actions on MySQL connection should be wrapped in a transaction
 * managed by higher level;
 * i.e. do not manage transactions within any action.
 * <p>
 * This interface is designed to be used in a mixin style, so you can extend it
 * to add your own methods.
 *
 * @see AbstractNamedMixinAction
 * @see AbstractNamedAction
 *
 * @param <C> a specific connection class that extends NamedMySQLConnection
 * @since 3.2.11
 */
public interface NamedActionInterface<C extends NamedMySQLConnection> {
    /**
     * Retrieves the associated named MySQL connection.
     *
     * @return the instance of the named MySQL connection linked to the implementing action; never null
     */
    @Nonnull
    C getNamedSqlConnection();
}
