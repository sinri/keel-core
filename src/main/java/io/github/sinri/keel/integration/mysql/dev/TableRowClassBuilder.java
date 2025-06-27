package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.integration.mysql.result.row.AbstractTableRow;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Date;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 * @since 3.1.7 Add deprecated table annotation.
 */
class TableRowClassBuilder {

    private final @Nonnull TableRowClassBuildOptions options;
    /**
     * @since 3.1.7
     */
    private boolean tableDeprecated = false;

    public TableRowClassBuilder(@Nonnull TableRowClassBuildOptions options) {
        this.options = options;
    }

    protected String parsedTableComment() {
        var tableComment = options.getTableComment();
        if (tableComment == null || tableComment.isBlank()) {
            return "Table comment is empty.";
        } else {
            // since 3.1.7
            String[] split = tableComment.split("@[Dd]eprecated", 2);
            if (split.length > 1) {
                // this table is deprecated
                this.tableDeprecated = true;
                return Keel.stringHelper().escapeForHttpEntity(split[1]);
            } else {
                return Keel.stringHelper().escapeForHttpEntity(tableComment);
            }
        }
    }

    public String getClassName() {
        return Keel.stringHelper().fromUnderScoreCaseToCamelCase(options.getTable()) + "TableRow";
    }

    public String build() {
        var table = options.getTable();
        var schema = options.getSchema();
        var className = getClassName();
        var vcsFriendly = options.isVcsFriendly();
        StringBuilder code = new StringBuilder();

        code.append("package ").append(options.getPackageName()).append(";").append("\n")
            .append("import ").append(AbstractTableRow.class.getName()).append(";\n")
            .append("import ").append(JsonObject.class.getName()).append(";\n")
            .append("\n")
            .append("import javax.annotation.Nonnull;\n")
            .append("import javax.annotation.Nullable;\n")
            .append("import java.util.Objects;\n")
            .append("\n")
            .append("/**\n")
            .append(" * ").append(parsedTableComment()).append("\n")
            .append(" * (´^ω^`)\n");
        if (schema != null && !schema.isBlank()) {
            code.append(" * SCHEMA: ").append(schema).append("\n");
        }
        code
                .append(" * TABLE: ").append(table).append("\n")
                .append(" * (*￣∇￣*)\n")
                .append(" * NOTICE BY KEEL:\n")
                .append(" * \tTo avoid being rewritten, do not modify this file manually.\n");
        if (!vcsFriendly) {
            code.append(" * \tIt was auto-generated on ").append(new Date()).append(".\n");
        }
        code.append(" * @see ").append(TableRowClassSourceCodeGenerator.class.getName()).append("\n")
            .append(" */\n");
        if (tableDeprecated) {
            code.append("@Deprecated\n");
        }
        code.append("public class ").append(className).append(" extends AbstractTableRow {").append("\n");

        if (schema != null && !schema.isBlank()) {
            if (this.options.isProvideConstSchema()) {
                code.append("\tpublic static final String SCHEMA = \"").append(schema).append("\";\n");
            }
            if (this.options.isProvideConstSchemaAndTable()) {
                code.append("\tpublic static final String SCHEMA_AND_TABLE = \"").append(schema).append(".")
                    .append(table).append("\";\n");
            }
        }
        if (this.options.isProvideConstTable()) {
            code.append("\tpublic static final String TABLE = \"").append(table).append("\";\n");
        }

        code
                .append("\n")
                .append("\t").append("public ").append(className).append("(JsonObject tableRow) {\n")
                .append("\t\tsuper(tableRow);\n")
                .append("\t}\n")
                .append("\n")
                .append("\t@Override\n")
                .append("\t@Nonnull\n")
                .append("\tpublic String sourceTableName() {\n")
                .append("\t\treturn ").append(this.options.isProvideConstTable() ? "TABLE" : "\"" + table + "\"")
                .append(";\n")
                .append("\t}\n")
                .append("\n");
        if (schema != null) {
            code.append("\tpublic String sourceSchemaName(){\n")
                .append("\t\treturn ").append(this.options.isProvideConstSchema() ? "SCHEMA" : "\"" + schema + "\"")
                .append(";\n")
                .append("\t}\n");
        }

        options.getFields().forEach(field -> code.append(field.toString()).append("\n"));

        code.append("\n}\n");

        var ddl = options.getDdl();
        if (ddl != null) {
            if (vcsFriendly) {
                // AUTO_INCREMENT=2395644
                var cleanedDDL = ddl.replaceAll("\\s+AUTO_INCREMENT=\\d+\\s+", " AUTO_INCREMENT=_ ");
                code.append("\n/*\n").append(cleanedDDL).append("\n */\n");
            } else {
                code.append("\n/*\n").append(ddl).append("\n */\n");
            }
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
