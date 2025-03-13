package io.github.sinri.keel.integration.mysql.statement.impl.ddl.view;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/drop-view.html">DROP VIEW Statement</a>
 * @since 4.0.4
 */
public class DropViewStatement extends AbstractStatement {
    private boolean ifExists = false;
    private final List<String> viewNames = new ArrayList<>();

    public DropViewStatement setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
        return this;
    }

    public DropViewStatement addViewName(String viewName) {
        this.viewNames.add(viewName);
        return this;
    }

    @Override
    public String toString() {
        /*
        DROP VIEW [IF EXISTS]
    view_name [, view_name] ...
    [RESTRICT | CASCADE]
         */
        // RESTRICT and CASCADE, if given, are parsed and ignored.
        List<String> list = viewNames.stream().map(x -> "`" + x + "`").collect(Collectors.toList());
        return "DROP VIEW "
                + (ifExists ? "IF EXISTS" : "") + " "
                + Keel.stringHelper().joinStringArray(list, ",");
    }
}
