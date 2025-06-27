package io.github.sinri.keel.integration.mysql.dev;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 4.1.0
 */
class TableRowClassBuildOptions extends TableRowClassBuildStandard {
    private final @Nonnull List<TableRowClassField> fields = new ArrayList<>();
    private String packageName;
    private @Nullable String schema;
    private String table;
    private @Nullable String tableComment;
    private @Nullable String ddl;

    public TableRowClassBuildOptions() {
        this(new TableRowClassBuildStandard());
    }

    public TableRowClassBuildOptions(TableRowClassBuildStandard standard) {
        super(standard);
    }

    public String getPackageName() {
        return packageName;
    }

    public TableRowClassBuildOptions setPackageName(@Nonnull String packageName) {
        this.packageName = packageName;
        return this;
    }

    @Nullable
    public String getSchema() {
        return schema;
    }

    public TableRowClassBuildOptions setSchema(@Nullable String schema) {
        this.schema = schema;
        return this;
    }

    public String getTable() {
        return table;
    }

    public TableRowClassBuildOptions setTable(@Nonnull String table) {
        this.table = table;
        return this;
    }


    @Nullable
    public String getTableComment() {
        return tableComment;
    }

    public TableRowClassBuildOptions setTableComment(@Nullable String tableComment) {
        this.tableComment = tableComment;
        return this;
    }

    @Nullable
    public String getDdl() {
        return ddl;
    }

    public TableRowClassBuildOptions setDdl(@Nullable String ddl) {
        this.ddl = ddl;
        return this;
    }

    public TableRowClassBuildOptions addField(TableRowClassField field) {
        this.fields.add(field);
        return this;
    }

    public TableRowClassBuildOptions addFields(@Nonnull List<TableRowClassField> fields) {
        this.fields.addAll(fields);
        return this;
    }

    @Nonnull
    public List<TableRowClassField> getFields() {
        return fields;
    }
}
