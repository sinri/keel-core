package io.github.sinri.keel.integration.mysql.statement.impl.ddl.view;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/alter-view.html">ALTER VIEW Statement</a>
 * @since 4.0.4
 */
public class AlterViewStatement extends AbstractStatement {
    /**
     * {@code UNDEFINED | MERGE | TEMPTABLE }
     */
    private @Nullable String algorithm = null;
    private @Nullable String definer = null;
    private @Nullable String sqlSecurity = null;
    private @Nonnull String viewName = "";
    private final List<String> columns = new ArrayList<>();
    private SelectStatement selectStatement;
    /**
     * {@code [WITH [CASCADED | LOCAL] CHECK OPTION]}
     */
    private @Nullable String others = null;

    public AlterViewStatement setAlgorithm(@Nullable String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public AlterViewStatement setDefiner(@Nullable String definer) {
        this.definer = definer;
        return this;
    }

    public AlterViewStatement setOthers(@Nullable String others) {
        this.others = others;
        return this;
    }

    public AlterViewStatement setSelectStatement(SelectStatement selectStatement) {
        this.selectStatement = selectStatement;
        return this;
    }

    public AlterViewStatement setViewName(@Nonnull String viewName) {
        this.viewName = viewName;
        return this;
    }

    public AlterViewStatement setSqlSecurity(@Nullable String sqlSecurity) {
        this.sqlSecurity = sqlSecurity;
        return this;
    }

    public AlterViewStatement addColumn(String columnName) {
        this.columns.add(columnName);
        return this;
    }

    @Override
    public String toString() {
        /*
        ALTER
    [ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = user]
    [SQL SECURITY { DEFINER | INVOKER }]
    VIEW view_name [(column_list)]
    AS select_statement
    [WITH [CASCADED | LOCAL] CHECK OPTION]
         */

        var columnsString = "";
        if (!columns.isEmpty()) {
            columnsString = "(" + Keel.stringHelper().joinStringArray(
                    columns.stream().map(x -> "`" + x + "`").collect(Collectors.toList()),
                    ", "
            ) + ")";
        }
        return "ALTER "
                + (algorithm != null ? ("ALGORITHM=" + algorithm) : "") + " "
                + (definer != null ? ("DEFINER=" + definer) : "") + " "
                + (sqlSecurity != null ? ("SQL SECURITY " + sqlSecurity) : "") + " "
                + "VIEW `" + viewName + "` "
                + columnsString
                + " AS " + selectStatement + " "
                + (others == null ? "" : others)
                ;
    }
}
