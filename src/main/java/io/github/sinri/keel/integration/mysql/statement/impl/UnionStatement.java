package io.github.sinri.keel.integration.mysql.statement.impl;


import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class UnionStatement extends AbstractStatement implements ReadStatementMixin {
    final List<String> selections = new ArrayList<>();

    public UnionStatement() {

    }

    public UnionStatement(@Nonnull String firstSelection) {
        selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + firstSelection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
    }

    public UnionStatement union(@Nonnull String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        }
        return this;
    }

    public UnionStatement unionAll(@Nonnull String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION ALL (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        }
        return this;
    }

    public UnionStatement union(@Nonnull List<String> list) {
        for (String selection : list) {
            union(selection);
        }
        return this;
    }

    public UnionStatement unionAll(@Nonnull List<String> list) {
        for (String selection : list) {
            unionAll(selection);
        }
        return this;
    }

    public String toString() {
        return Keel.stringHelper().joinStringArray(selections, " ") + (
                getRemarkAsComment().isEmpty() ? "" : ("\n-- " + getRemarkAsComment())
        );
    }

}
